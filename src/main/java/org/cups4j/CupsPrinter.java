package org.cups4j;

import ch.ethz.vppserver.ippclient.IppResult;
import lombok.Getter;
import lombok.Setter;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.operations.ipp.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Represents a printer on your IPP server
 */
@Setter
@Getter
public class CupsPrinter {

    private final CupsAuthentication creds;
    private final String name;
    private URL printerURL;
    private PrinterStateEnum state = null;
    private String description = null;
    private String location = null;
    private boolean isDefault = false;
    private boolean printerClass = false;
    private String mediaDefault = null;
    private String resolutionDefault = null;
    private String colorModeDefault = null;
    private String sidesDefault = null;
    private String deviceUri = null;
    private String printerState = null;
    private String printerStateMessage = null;
    private String printerStateReasons = null;
    private String numberUpDefault = null;
    private List<String> numberUpSupported = new ArrayList<>();
    private List<String> mediaSupported = new ArrayList<>();
    private List<String> resolutionSupported = new ArrayList<>();
    private List<String> colorModeSupported = new ArrayList<>();
    private List<String> mimeTypesSupported = new ArrayList<>();
    private List<String> sidesSupported = new ArrayList<>();
    private String makeAndModel = null;

    public CupsPrinter(CupsAuthentication creds, URL printerURL, String printerName) {
        super();
        this.creds = creds;
        this.printerURL = printerURL;
        this.name = printerName;
    }

    private static void verifyUser(String userName, PrintJob[] printJobs) {
        for (PrintJob job : printJobs) {
            String jobUserName = job.getUserName();
            if (!userName.equals(jobUserName)) {
                throw new IllegalStateException(
                        "different users (" + userName + ", " + jobUserName + ", ...) in print jobs are forbidden");
            }
        }
    }

    public boolean isPrinterClass() {
        return printerClass || (printerURL != null && printerURL.toString().contains("class"));
    }

    public PrintRequestResult print(PrintJob printJob) throws Exception {
        int ippJobID = -1;
        InputStream document = printJob.getDocument();
        String userName = printJob.getUserName();
        String jobName = printJob.getJobName();
        int copies = printJob.getCopies();
        String pageRanges = printJob.getPageRanges();
        String resolution = printJob.getResolution();

        String pageFormat = printJob.getPageFormat();
        boolean color = printJob.isColor();
        boolean portrait = printJob.isPortrait();

        Map<String, String> attributes = printJob.getAttributes();

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER;
        }

        attributes.put("requesting-user-name", userName);
        attributes.put("job-name", jobName);

        String copiesString;
        StringBuilder rangesString = new StringBuilder();
        if (copies > 0) { // other values are considered bad value by CUPS
            copiesString = "copies:integer:" + copies;
            addAttribute(attributes, "job-attributes", copiesString);
        }
        addAttribute(attributes, "job-attributes", portrait ? "orientation-requested:enum:3" : "orientation-requested:enum:4");
        addAttribute(attributes, "job-attributes", color ? "output-mode:keyword:color" : "output-mode:keyword:monochrome");

        if (isNotEmpty(pageFormat)) {
            addAttribute(attributes, "job-attributes", "media:keyword:" + pageFormat);
        }

        if (isNotEmpty(resolution)) {
            addAttribute(attributes, "job-attributes", "printer-resolution:resolution:" + resolution);
        }

        if (isNotBlank(pageRanges) && !"1-".equals(pageRanges.trim())) {
            String[] ranges = pageRanges.split(",");

            String delimeter = "";

            rangesString.append("page-ranges:setOfRangeOfInteger:");
            for (String range : ranges) {
                range = range.trim();

                String[] values = range.split("-");
                if (values.length == 1) {
                    range = range + "-" + range;
                }

                rangesString.append(delimeter).append(range);
                // following ranges need to be separated with ","
                delimeter = ",";
            }
            addAttribute(attributes, "job-attributes", rangesString.toString());
        }

        if (printJob.isDuplex()) {
            addAttribute(attributes, "job-attributes", "sides:keyword:two-sided-long-edge");
        }
        IppPrintJobOperation command = new IppPrintJobOperation(printerURL.getPort());
        IppResult ippResult = command.request(this, printerURL, attributes, document, creds);
        PrintRequestResult result = new PrintRequestResult(ippResult);
        // IppResultPrinter.print(result);

        for (AttributeGroup group : ippResult.getAttributeGroupList()) {
            if (group.getTagName().equals("job-attributes-tag")) {
                for (Attribute attr : group.getAttributes()) {
                    if (attr.getName().equals("job-id")) {
                        ippJobID = Integer.parseInt(attr.getAttributeValues().get(0).getValue());
                    }
                }
            }
        }
        result.setJobId(ippJobID);
        return result;
    }

    /**
     * Print method for several print jobs which should be not interrupted by
     * another print job. The printer must support
     * 'multiple-document-jobs-supported' which is a recommended option.
     * <p>
     * ATTENTION: Don't use different users for the different print jobs. You will
     * get probably error 401 (forbidden) from CUPS. To avoid error 401 you'll get
     * now an {@link IllegalStateException}.
     * </p>
     *
     * @param job1     first print job
     * @param moreJobs more print jobs
     * @return PrintRequestResult
     * @author oboehm
     * @since 0.7.2
     */
    public PrintRequestResult print(PrintJob job1, PrintJob... moreJobs) {
        verifyUser(job1.getUserName(), moreJobs);
        int jobId = createJob(job1);
        List<PrintJob> printJobs = new ArrayList<>();
        printJobs.add(job1);
        printJobs.addAll(Arrays.asList(moreJobs));
        for (int i = 0; i < printJobs.size() - 1; i++) {
            print(printJobs.get(i), jobId, false);
        }
        return print(printJobs.get(printJobs.size() - 1), jobId, true);
    }

    /**
     * If you want to print serveral print jobs as one job you must first tell
     * CUPS that you want to start. This is the method to create a job. The
     * returned job-id must be used for the following print calls.
     *
     * @param jobName the name of a job
     * @return the job-id
     * @author oboehm
     * @since 0.7.2
     * @deprecated use {@link #createJob(PrintJob)} or
     * {@link #createJob(String, String)}
     */
    @Deprecated
    public int createJob(String jobName) {
        return createJob(jobName, CupsClient.DEFAULT_USER);
    }

    /**
     * If you want to print serveral print jobs as one job you must first tell
     * CUPS that you want to start. This is the method to create a job. The
     * returned job-id must be used for the following print calls.
     *
     * @param jobName  the name of a job
     * @param userName the name of a user
     * @return the job-id
     * @author oboehm
     * @since 0.7.4
     */
    public int createJob(String jobName, String userName) {
        return createJob(PrintJob.builder().document(new ByteArrayInputStream(new byte[0]))
                .jobName(jobName).userName(userName).build());
    }

    /**
     * If you want to print serveral print jobs as one job you must first tell
     * CUPS that you want to start. This is the method to create a job. The
     * returned job-id must be used for the following print calls.
     *
     * @param job the print-job with job-name and user-name
     * @return the job-id
     * @author oboehm
     * @since 0.7.4
     */
    public int createJob(PrintJob job) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("job-name", job.getJobName());
        attributes.put("requesting-user-name", job.getUserName());
        IppCreateJobOperation command = new IppCreateJobOperation(printerURL.getPort());
        IppResult ippResult = command.request(this, printerURL, attributes, creds);
        if (ippResult.isPrintQueueUnavailable()) {
            throw new IllegalStateException("The print queue is not available: " + ippResult.getIppStatusResponse());
        }
        AttributeGroup attrGroup = ippResult.getAttributeGroup("job-attributes-tag");
        return Integer.parseInt(attrGroup.getAttributes("job-id").getValue());
    }

    /**
     * Call this method if you want to print several print jobs as one print job.
     * Call {@link #createJob(String)} to the get the correct job-id.
     *
     * @param job          the job
     * @param jobId        the job id from {@link #createJob(String)}
     * @param lastDocument set it to true if it is the last document
     * @return the print request result
     * @author oboehm
     * @since 0.7.2
     */
    public PrintRequestResult print(PrintJob job, int jobId, boolean lastDocument) {
        IppSendDocumentOperation op = new IppSendDocumentOperation(printerURL.getPort(), jobId, lastDocument);
        IppResult ippResult = op.request(this, printerURL, job, creds);
        PrintRequestResult result = new PrintRequestResult(ippResult);
        result.setJobId(jobId);
        return result;
    }

    private void addAttribute(Map<String, String> map, String name, String value) {
        if (value != null && name != null) {
            if (map.containsKey(name)) {
                map.put(name, map.get(name) + "#" + value);
            } else {
                map.put(name, value);
            }
        }
    }

    /**
     * Get a list of jobs
     *
     * @param whichJobs completed, not completed or all
     * @param user      requesting user (null will be translated to anonymous)
     * @param myJobs    boolean only jobs for requesting user or all jobs for this
     *                  printer?
     * @return job list
     * @throws Exception
     */

    public List<PrintJobAttributes> getJobs(WhichJobsEnum whichJobs, String user, boolean myJobs) throws Exception {
        IppGetJobsOperation command = new IppGetJobsOperation(printerURL.getPort());
        return command.getPrintJobs(this, whichJobs, user, myJobs, creds);
    }

    /**
     * Get current status for the print job with the given ID.
     *
     * @param jobID
     * @return job status
     * @throws Exception
     */
    public JobStateEnum getJobStatus(int jobID) throws Exception {
        return getJobStatus(CupsClient.DEFAULT_USER, jobID);
    }

    /**
     * Get current status for the print job with the given ID
     *
     * @param userName
     * @param jobID
     * @return job status
     * @throws Exception
     */
    public JobStateEnum getJobStatus(String userName, int jobID) throws Exception {
        IppGetJobAttributesOperation command = new IppGetJobAttributesOperation(printerURL.getPort());
        PrintJobAttributes job = command.getPrintJobAttributes(printerURL.getHost(), userName, jobID, creds);
        return job.getJobState();
    }

    /**
     * Get a String representation of this printer consisting of the printer URL
     * and the name
     *
     * @return String
     */
    public String toString() {
        return name;
    }

}

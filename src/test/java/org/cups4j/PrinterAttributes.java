package org.cups4j;

import ch.ethz.vppserver.ippclient.IppResult;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.ipp.attributes.Attribute;
import org.cups4j.ipp.attributes.AttributeGroup;
import org.cups4j.ipp.attributes.AttributeValue;
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
public class PrinterAttributes {
    private final JTabbedPane mainTab = new JTabbedPane();
    private String hostname = "localhost";

    public PrinterAttributes(String host) {
        try {
            if (host != null)
                hostname = host;

            JFrame frame = new JFrame("Drucker auf " + hostname);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(mainTab);

            List<CupsPrinter> printers = new CupsClient().getPrinters();

            Map<String, String> map = new HashMap<>();
            map.put("requested-attributes", "all");

            for (CupsPrinter p : printers) {
                IppGetPrinterAttributesOperation o = new IppGetPrinterAttributesOperation();
                IppResult result = o.request(p, p.getPrinterURL(), map, new CupsAuthentication("anonymous", "anonymous"));
                // IppResultPrinter.print(result);
                addPrinterPanel(p.getName(), result);
            }

            frame.setVisible(true);
        } catch (Exception e) {
            log.error("Startup failure", e);
        }
    }

    public static void main(String[] args) {
        new PrinterAttributes((args.length > 0) ? args[0] : null);
    }

    private void addPrinterPanel(String name, IppResult result) {
        mainTab.add(getPrinterPanel(result), name);
    }

    private Container getPrinterPanel(IppResult result) {
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        JTabbedPane tab = new JTabbedPane();

        for (AttributeGroup group : result.getAttributeGroupList()) {
            if (isNotEmpty(group.getAttributes())) {
                tab.add(gatAttributeTab(group), group.getTagName());
            }
        }

        jp.add(tab, BorderLayout.CENTER);
        return jp;
    }

    private Component gatAttributeTab(AttributeGroup group) {
        JPanel jp = new JPanel(new BorderLayout());
        ScrollPane scp = new ScrollPane();
        jp.add(scp, BorderLayout.CENTER);

        FormLayout layout = new FormLayout("12dlu, pref, 6dlu, 30dlu:grow, 3dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.leadingColumnOffset(1);

        group.getAttributes().sort(Comparator.comparing(Attribute::getName));

        for (Attribute att : group.getAttributes()) {
            JComponent valueComponent;
            if (isNotEmpty(att.getAttributeValues())) {
                JPanel panel = new JPanel(new BorderLayout());

                AttributeValueTable table = new AttributeValueTable((getAttributeTableModel(att.getAttributeValues())));
                panel.add(table.getTableHeader(), BorderLayout.NORTH);
                panel.add(table, BorderLayout.CENTER);
                valueComponent = panel;

            } else {
                JLabel lb = new JLabel("no value reported");
                lb.setForeground(Color.red);
                valueComponent = lb;
            }
            builder.appendSeparator();
            builder.append(att.getName(), valueComponent);
            builder.nextLine();
        }
        scp.add(builder.getPanel());

        return jp;
    }

    private DefaultTableModel getAttributeTableModel(List<AttributeValue> list) {
        Vector<Vector<String>> data = new Vector<>();
        Vector<String> names = new Vector<>();
        names.add("Tag Name");
        names.add("Tag (Hex)");
        names.add("Tag Value");
        for (AttributeValue attrValue : list) {
            data.add(getAttributeValue(attrValue));
        }
        return new DefaultTableModel(data, names);

    }

    private Vector<String> getAttributeValue(AttributeValue attrValue) {
        Vector<String> values = new Vector<>();
        values.add(attrValue.getTagName());
        values.add(attrValue.getTag());
        values.add(attrValue.getValue());

        return values;
    }

    public static class AttributeValueTable extends JTable {
        private static final long serialVersionUID = -9079318497719930285L;

        public AttributeValueTable(TableModel model) {
            super(model);
            TableColumnModel colmodel = getColumnModel();

            // Set column widths
            colmodel.getColumn(0).setPreferredWidth(100);
            colmodel.getColumn(1).setPreferredWidth(30);
            colmodel.getColumn(2).setPreferredWidth(150);
        }
    }

}

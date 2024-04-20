package org.cups4j.operations;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
class IppHttpResult {

    private String statusLine;

    private int statusCode;

}

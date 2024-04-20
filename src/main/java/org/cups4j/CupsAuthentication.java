package org.cups4j;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CupsAuthentication {

    private final String userid;

    private final String password;

}

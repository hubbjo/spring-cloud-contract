/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.junit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class StubRunnerJUnit5ExtensionCustomPortTests {

    @BeforeAll
    @AfterAll
    static void setupProps() {
        System.clearProperty("stubrunner.repository.root");
        System.clearProperty("stubrunner.classifier");
    }

    @RegisterExtension
    static StubRunnerExtension stubRunnerExtension = new StubRunnerExtension()
            .repoRoot(repoRoot())
            .stubsMode(StubRunnerProperties.StubsMode.REMOTE)
            .downloadStub("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")
            .withPort(12345)
            .downloadStub("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer:12346");

    @Test
    void should_start_wiremock_servers() throws Exception {
        then(stubRunnerExtension.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")).isNotNull();
        then(stubRunnerExtension.findStubUrl("loanIssuance")).isNotNull();
        then(stubRunnerExtension.findStubUrl("loanIssuance"))
                .isEqualTo(stubRunnerExtension.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance"));
        then(stubRunnerExtension.findStubUrl("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")).isNotNull();
        then(stubRunnerExtension.findAllRunningStubs().isPresent("loanIssuance")).isTrue();
        then(stubRunnerExtension.findAllRunningStubs()
                .isPresent("org.springframework.cloud.contract.verifier.stubs", "fraudDetectionServer")).isTrue();
        then(stubRunnerExtension.findAllRunningStubs()
                .isPresent("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")).isTrue();
        then(httpGet(stubRunnerExtension.findStubUrl("loanIssuance").toString() + "/name")).isEqualTo("loanIssuance");
        then(httpGet(stubRunnerExtension.findStubUrl("fraudDetectionServer").toString() + "/name")).isEqualTo("fraudDetectionServer");
        then(stubRunnerExtension.findStubUrl("loanIssuance")).isEqualTo(URI.create("http://localhost:12345").toURL());
        then(stubRunnerExtension.findStubUrl("fraudDetectionServer")).isEqualTo(URI.create("http://localhost:12346").toURL());
    }

    private static String repoRoot() {
        try {
            return StubRunnerJUnit5ExtensionCustomPortTests.class.getResource("/m2repo/repository/")
                    .toURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String httpGet(String url) throws Exception {
        try (InputStream stream = URI.create(url).toURL().openStream()) {
            return StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
        }
    }
}

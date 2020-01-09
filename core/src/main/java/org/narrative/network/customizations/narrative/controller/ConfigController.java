package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/24/18
 * Time: 11:11 PM
 *
 * @author brian
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
    @GetMapping(path = "/recaptcha-public-key")
    public ScalarResultDTO<String> getRecaptchaApiPublicKey() {
        return ScalarResultDTO.<String>builder().value(NetworkRegistry.getInstance().getReCaptchaPublicKey()).build();
    }

    @GetMapping(path = "/notice-url")
    public ScalarResultDTO<String> getShutdownNoticeUrl() {
        return ScalarResultDTO.<String>builder().value(areaContext().getAreaRlm().getSandboxedCommunitySettings().getShutdownNoticeUrl()).build();
    }
}

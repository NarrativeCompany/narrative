package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.customizations.narrative.service.api.NeoWalletService;
import org.narrative.network.customizations.narrative.service.api.model.NeoWalletDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Date: 10/1/19
 * Time: 1:11 PM
 *
 * @author brian
 */
@RestController
@RequestMapping("/neo-wallets")
public class NeoWalletController {
    private final NeoWalletService neoWalletService;

    public NeoWalletController(NeoWalletService neoWalletService) {
        this.neoWalletService = neoWalletService;
    }

    @GetMapping
    public List<NeoWalletDTO> getNeoWallets() {
        return neoWalletService.getNeoWallets();
    }
}

package org.narrative.network.customizations.narrative.neo.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 7:57 AM
 */
public class NeoUtils {
    public static final int NEO_ADDRESS_LENGTH = 34;
    public static final int NEO_RAW_ADDRESS_BYTE_LENGTH = 21;
    public static final int NEO_ADDRESS_VERSION_BYTE = 23;
    public static final Pattern NEO_SCRIPT_HASH_PATTERN = Pattern.compile("[0-9a-f]{40}", Pattern.CASE_INSENSITIVE);
    public static final int NEO_TRANSACTION_ID_LENGTH = 64;
    public static final Pattern NEO_TRANSACTION_ID_PATTERN = Pattern.compile("[0-9a-f]{" + NEO_TRANSACTION_ID_LENGTH + "}", Pattern.CASE_INSENSITIVE);

    public static boolean isValidAddress(String address) {
        try {
            byte[] bytes = Base58Check.decode(address);
            if (bytes == null) {
                return false;
            }
            //  bl: the address is 160 bits, so 20 bytes, plus one leading version byte.
            if (bytes.length != NEO_RAW_ADDRESS_BYTE_LENGTH) {
                return false;
            }
            // bl: the version byte is 23
            // per AddressVersion: https://github.com/neo-project/neo/blob/master/neo/protocol.json
            if (bytes[0] != NEO_ADDRESS_VERSION_BYTE) {
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw UnexpectedError.getRuntimeException("NoSuchAlgorithmException shouldn't be possible! No SHA-256?", e);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isProperAddressLength(String address) {
        // bl: must be exactly 34 characters
        return address != null && address.length() == NEO_ADDRESS_LENGTH;
    }

    public static boolean validateNeoAddress(ValidationHandler handler, String paramName, String fieldLabel, String neoAddress) {
        if (handler.validateNotEmptyWithLabel(neoAddress, paramName, fieldLabel)) {
            if (!isProperAddressLength(neoAddress)) {
                handler.addWordletizedFieldError(paramName, "neoFieldsHelper.neoAddressLength");
            } else if (!isValidAddress(neoAddress)) {
                handler.addWordletizedFieldError(paramName, "neoFieldsHelper.neoAddressInvalid");
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean validateNeoAddress(ValidationContext validationContext, String fieldName, String neoAddress) {
        if (!isProperAddressLength(neoAddress)) {
            validationContext.addFieldError(fieldName, wordlet("neoFieldsHelper.neoAddressLength"));
        } else if (!isValidAddress(neoAddress)) {
            validationContext.addFieldError(fieldName, wordlet("neoFieldsHelper.neoAddressInvalid"));
        } else {
            return true;
        }

        return false;
    }

    public static boolean validateNeoScriptHash(ValidationHandler handler, String paramName, String fieldLabel, String nrveScriptHash) {
        if (handler.validateNotEmptyWithLabel(nrveScriptHash, paramName, fieldLabel)) {
            return handler.validateStringWithLabel(nrveScriptHash, NEO_SCRIPT_HASH_PATTERN, paramName, fieldLabel);
        }
        return false;
    }

    public static boolean validateNeoTransactionId(ValidationHandler handler, String paramName, String fieldLabel, String transactionId) {
        if (handler.validateNotEmptyWithLabel(transactionId, paramName, fieldLabel)) {
            return handler.validateStringWithLabel(transactionId, NEO_TRANSACTION_ID_PATTERN, paramName, fieldLabel);
        }
        return false;
    }

    public static boolean isValidNeoTransactionId(String transactionId) {
        return !isEmpty(transactionId) && NEO_TRANSACTION_ID_PATTERN.matcher(transactionId).matches();
    }
}

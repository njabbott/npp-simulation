package com.nick.npp.dto;

import com.nick.npp.model.PayIdType;

public record PayIdResolutionResponse(
        PayIdType payIdType,
        String value,
        String displayName,
        String bsb,
        String accountNumber,
        String bankName,
        String bankBic
) {}

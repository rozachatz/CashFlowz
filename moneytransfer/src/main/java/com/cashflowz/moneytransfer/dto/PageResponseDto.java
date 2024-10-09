package com.cashflowz.moneytransfer.dto;

import java.util.List;

/**
 * Dto for representing a page response.
 *
 * @param <T> The type of content contained in the page response.
 */
public record PageResponseDto<T>(List<T> content) {
}

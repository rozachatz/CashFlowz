package com.moneytransfer.dto;

import java.util.List;

/**
 * Dto for representing a page response.
 *
 * @param <T> The type of entities contained in the page response.
 */
public record PageResponseDto<T>(List<T> entities) {
}

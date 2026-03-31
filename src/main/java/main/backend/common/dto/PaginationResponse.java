package main.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> items;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }

    public static <T> PaginationResponse<T> of(List<T> items, int page, int limit, long total) {
        return PaginationResponse.<T>builder()
                .items(items)
                .pagination(PaginationInfo.builder()
                        .page(page)
                        .limit(limit)
                        .total(total)
                        .totalPages((int) Math.ceil((double) total / limit))
                        .build())
                .build();
    }
}

package io.example.role.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllRolesRequest {
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int pageSize = 10;
    private String search;
}

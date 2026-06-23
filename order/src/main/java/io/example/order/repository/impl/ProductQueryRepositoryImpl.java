package io.example.order.repository.impl;

import io.example.order.model.ProductInfo;
import io.example.order.repository.ProductQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.VertxProductQueryServiceGrpcClient;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final VertxProductQueryServiceGrpcClient client;

    @Override
    public Future<ProductInfo> findById(Integer productId) {
        FindByIdProductRequest request = FindByIdProductRequest.newBuilder()
                .setId(productId)
                .build();

        return client.findById(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var p = response.getData();
                        return ProductInfo.builder()
                                .productId((long) p.getId())
                                .name(p.getName())
                                .price(p.getPrice())
                                .imageProduct(p.getImageProduct())
                                .weight(p.getWeight())
                                .countInStock(p.getCountInStock())
                                .build();
                    }
                    return null;
                });
    }
}

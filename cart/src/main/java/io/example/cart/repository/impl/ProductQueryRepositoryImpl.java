package io.example.cart.repository.impl;

import io.example.cart.model.ProductInfo;
import io.example.cart.repository.ProductQueryRepository;
import io.vertx.core.Future;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.VertxProductQueryServiceGrpcClient;

public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final VertxProductQueryServiceGrpcClient client;

    public ProductQueryRepositoryImpl(VertxProductQueryServiceGrpcClient client) {
        this.client = client;
    }

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
                                .build();
                    }
                    return null;
                });
    }
}

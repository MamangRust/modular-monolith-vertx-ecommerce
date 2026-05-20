package io.example.cart.handler;

public class ProtoConverter {

    public static pb.cart.CartCommon.CartResponse toProto(io.example.cart.model.CartResponse r) {
        if (r == null) {
            return pb.cart.CartCommon.CartResponse.getDefaultInstance();
        }
        return pb.cart.CartCommon.CartResponse.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setPrice(r.getPrice() != null ? r.getPrice() : 0)
                .setImage(r.getImage() != null ? r.getImage() : "")
                .setQuantity(r.getQuantity() != null ? r.getQuantity() : 0)
                .setWeight(r.getWeight() != null ? r.getWeight() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }
}

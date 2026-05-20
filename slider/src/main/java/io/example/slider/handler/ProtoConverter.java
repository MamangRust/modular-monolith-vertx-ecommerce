package io.example.slider.handler;

import com.google.protobuf.StringValue;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;

public class ProtoConverter {

    public static pb.slider.SliderCommon.SliderResponse toProtoResponse(SliderResponse s) {
        if (s == null) {
            return pb.slider.SliderCommon.SliderResponse.getDefaultInstance();
        }
        return pb.slider.SliderCommon.SliderResponse.newBuilder()
                .setId(s.getId().intValue())
                .setName(s.getName() != null ? s.getName() : "")
                .setImage(s.getImage() != null ? s.getImage() : "")
                .setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt() : "")
                .setUpdatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt() : "")
                .build();
    }

    public static pb.slider.SliderCommon.SliderResponseDeleteAt toProtoResponseDeleteAt(SliderResponseDeleteAt s) {
        if (s == null) {
            return pb.slider.SliderCommon.SliderResponseDeleteAt.getDefaultInstance();
        }
        pb.slider.SliderCommon.SliderResponseDeleteAt.Builder b = pb.slider.SliderCommon.SliderResponseDeleteAt.newBuilder()
                .setId(s.getId().intValue())
                .setName(s.getName() != null ? s.getName() : "")
                .setImage(s.getImage() != null ? s.getImage() : "")
                .setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt() : "")
                .setUpdatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt() : "");

        if (s.getDeletedAt() != null && !s.getDeletedAt().isEmpty()) {
            b.setDeletedAt(StringValue.of(s.getDeletedAt()));
        }
        return b.build();
    }
}

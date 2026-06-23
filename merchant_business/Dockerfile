# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY proto/ proto/
COPY apigateway/pom.xml apigateway/
COPY auth/pom.xml auth/
COPY banner/pom.xml banner/
COPY cart/pom.xml cart/
COPY category/pom.xml category/
COPY email/pom.xml email/
COPY merchant/pom.xml merchant/
COPY merchant_award/pom.xml merchant_award/
COPY merchant_business/pom.xml merchant_business/
COPY merchant_detail/pom.xml merchant_detail/
COPY merchant_policy/pom.xml merchant_policy/
COPY order/pom.xml order/
COPY order_item/pom.xml order_item/
COPY product/pom.xml product/
COPY review/pom.xml review/
COPY review_detail/pom.xml review_detail/
COPY role/pom.xml role/
COPY shipping_address/pom.xml shipping_address/
COPY slider/pom.xml slider/
COPY transaction/pom.xml transaction/
COPY user/pom.xml user/
COPY db-migration/pom.xml db-migration/

COPY common/src common/src
COPY merchant_business/src merchant_business/src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl merchant_business -am -DskipTests -q -Dmaven.wagon.http.retryHandler.count=5

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/merchant_business/target/merchant_business-*-fat.jar app.jar
COPY chaos.yaml .
EXPOSE 50066
ENTRYPOINT ["java", "-jar", "app.jar"]

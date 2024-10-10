package PibuStory.skin;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Cosmetic {

    private String id;
    private String product;
    private String image_link;
    private String brand;
    private int price_after;
    private int price_before;
    private List<String> ingredient;
    private int review_count;
    private double rating_average;
    private double star_5;
    private double dry;
    private double combination;
    private double oily;

    // 모든 화장품 속성 포함한 DTO

    // Getter

    public String getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public String getImage_link() {
        return image_link;
    }

    public String getBrand() {
        return brand;
    }

    public int getPrice_after() {
        return price_after;
    }

    public int getPrice_before() {
        return price_before;
    }

    public List<String> getIngredient() {
        return ingredient;
    }

    public int getReview_count() {
        return review_count;
    }

    public double getRating_average() {
        return rating_average;
    }

    public double getStar_5() {
        return star_5;
    }

    public double getDry() {
        return dry;
    }

    public double getCombination() {
        return combination;
    }

    public double getOily() {
        return oily;
    }


    // Setter

    public void setId(String id) {
        this.id = id;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrice_after(int price_after) {
        this.price_after = price_after;
    }

    public void setPrice_before(int price_before) {
        this.price_before = price_before;
    }

    public void setIngredient(List<String> ingredient) {
        this.ingredient = ingredient;
    }

    public void setReview_count(int review_count) {
        this.review_count = review_count;
    }

    public void setRating_average(double rating_average) {
        this.rating_average = rating_average;
    }

    public void setStar_5(double star_5) {
        this.star_5 = star_5;
    }

    public void setDry(double dry) {
        this.dry = dry;
    }

    public void setCombination(double combination) {
        this.combination = combination;
    }

    public void setOily(double oily) {
        this.oily = oily;
    }
}
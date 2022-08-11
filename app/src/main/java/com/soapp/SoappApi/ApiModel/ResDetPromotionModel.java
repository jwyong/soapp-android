package com.soapp.SoappApi.ApiModel;

import java.util.List;

/* Created by Soapp on 30/10/2017. */

public class ResDetPromotionModel {
    private List<ResDetPromoTypeModel> promotion_restaurant_simple_discount;

    public List<ResDetPromoTypeModel> getPromotion_restaurant_simple_discount() {
        return promotion_restaurant_simple_discount;
    }

    public void setPromotion_restaurant_simple_discount(List<ResDetPromoTypeModel> promotion_restaurant_simple_discount) {
        this.promotion_restaurant_simple_discount = promotion_restaurant_simple_discount;
    }
}

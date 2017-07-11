package com.inszva.no0sdk;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by InsZVA on 2017/7/10.
 */

public class Payment {
    public Long paymentId;
    public String paymentNumber;
    public List<ItemPair> paymentItems;
    public Long paymentUserId;
    public Long paymentUavId;
    public double paymentPrice;
    public Long paymentTime;
}

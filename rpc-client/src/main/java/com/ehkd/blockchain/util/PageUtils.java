package com.ehkd.blockchain.util;

import java.math.BigDecimal;
import java.util.List;

public class PageUtils {

    public static <T> List toPageList(List<T>list, Integer pageNumber, Integer pageSize) {
        if(list!=null&&list.size()>0) {
            pageNumber = pageNumber>0?pageNumber:1;
            pageSize = pageSize>0?pageSize:1;
            Integer start = pageSize * (pageNumber -1);
            if(start>list.size()) {
                return null;
            }
            Integer end = pageSize * pageNumber;
            if(end > list.size()) {
                end = list.size();
            }
            return list.subList(start, end);
        }
        return null;
    }

}

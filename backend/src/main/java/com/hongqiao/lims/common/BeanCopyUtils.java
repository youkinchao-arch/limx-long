package com.hongqiao.lims.common;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public final class BeanCopyUtils {

    private static final List<String> ALWAYS_IGNORED = List.of("id", "createdAt", "updatedAt");

    private BeanCopyUtils() {}

    /** Copy only non-null properties from {@code src} onto {@code target}, never touching id/timestamps. */
    public static void copyNonNull(Object src, Object target) {
        BeanWrapper wrapper = new BeanWrapperImpl(src);
        List<String> ignored = new ArrayList<>(ALWAYS_IGNORED);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (wrapper.getPropertyValue(pd.getName()) == null) {
                ignored.add(pd.getName());
            }
        }
        BeanUtils.copyProperties(src, target, ignored.toArray(new String[0]));
    }
}

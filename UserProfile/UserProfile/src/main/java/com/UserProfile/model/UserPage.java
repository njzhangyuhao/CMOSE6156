package com.UserProfile.model;

import org.springframework.data.domain.Sort;

public class UserPage {
    private int pageNumber =0;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if(pageSize<this.pageSize)
            this.pageSize = pageSize;
    }

    private int pageSize = 10;
    private Sort sort = Sort.unsorted();
}

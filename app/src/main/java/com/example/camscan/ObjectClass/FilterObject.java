package com.example.camscan.ObjectClass;

public class FilterObject
{
    private  int image_id;
    private  String filter_name;
    public  FilterObject(int image_id,String filter_name)
    {
     this.image_id=image_id;
     this.filter_name=filter_name;
    }

    public int getImage_id() {
        return image_id;
    }

    public String getFilter_name() {
        return filter_name;
    }
}

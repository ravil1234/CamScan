package com.example.camscan.ObjectClass;

import com.example.camscan.AdapterClass.GridViewImages;

public class GridViewImagesList
{
    private  String image_url,image_date;
    public GridViewImagesList(String image_url,String image_date)
    {
        this.image_url=image_url;
        this.image_date=image_date;
    }
    public  String getImage_url()
    {
        return  image_url;
    }
    public  String getImage_date()
    {
        return  image_date;
    }
}

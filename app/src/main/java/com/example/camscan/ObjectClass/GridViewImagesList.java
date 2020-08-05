package com.example.camscan.ObjectClass;
public class GridViewImagesList
{
    private  String image_url,image_date,image_edited_date;
    private  int did,pcount;
    public GridViewImagesList(int did,String image_url,String image_date,int pcount,String image_edited_date)
    {
        this.did=did;
        this.image_url=image_url;
        this.image_date=image_date;
        this.pcount=pcount;
        this.image_edited_date=image_edited_date;
    }
    public  String getImage_url()
    {
        return  image_url;
    }
    public  String getImage_date()
    {
        return  image_date;
    }
    public  String getImage_edited_date()
    {
        return  image_edited_date;
    }
    public  int getDid()
    {
        return  did;
    }
    public  int getPcount()
    {
        return  pcount;
    }
}

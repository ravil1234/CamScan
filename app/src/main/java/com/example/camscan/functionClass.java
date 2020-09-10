package com.example.camscan;



public class functionClass {
/*
    private void shareSelected(final ArrayList<Integer> dids){
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeScreenActivity.this);
        ProgressBar v=new ProgressBar(HomeScreenActivity.this, null,android.R.attr.progressBarStyleHorizontal);
        v.setIndeterminate(false);
        v.setMax(100);
        builder.setTitle("In Progress . . .");
        builder.setView(v);
        builder.setCancelable(false);
        AlertDialog d=builder.create();
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                final int[] a = {0};
                int each=(int)(100/dids.size());
                ArrayList<Uri> pdfUris=new ArrayList<>();
                MyDatabase db=MyDatabase.getInstance(HomeScreenActivity.this);
                for(int did:dids){
                    MyDocument doc=db.myDocumentDao().getDocumentWithId(did);
                    String docName=doc.getDname();
                    List<MyPicture> pics=db.myPicDao().getDocPics(did);
                    ArrayList<Uri> picUri=new ArrayList<>();
                    if(pics!=null){
                        for(MyPicture p:pics){
                            picUri.add(Uri.parse(p.getEditedUri()));
                        }
                    }
                    MyCustomPdf pdf = new MyCustomPdf(HomeScreenActivity.this, picUri, false);
                    Uri savedPdf=pdf.savePdf2(docName, null, new MyDocumentActivity.pdfProgress() {
                        @Override
                        public void onUpdate(int perc) {
                            //Log.e("THIS", "onUpdate: "+perc );

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Log.e("TAG", "run: "+perc );
                                    if(perc==100){
                                        a[0] +=each;
                                        v.setProgress(a[0]);
                                    }
                                }
                            });

                        }
                    });
                    if (savedPdf!=null) {
                        //Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                        Log.e("PDF MY DOCACTIVITY", "savePdf: "+"PDF SAVED" );
                        pdfUris.add(savedPdf);
                    } else {
                        Log.e("PDF MY DOCACTIVITY", "Pdf Saving failed "+did );

                    }

                }
                if(pdfUris.size()<dids.size()){
                    //all are not converted
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setProgress(100);
                            d.dismiss();
                            Toast.makeText(HomeScreenActivity.this, "Few Docs Are skipped", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Documents From "+UtilityClass.appName);
                intent.setType("application/pdf"); /

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, pdfUris);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                    }
                });



            }
        }).start();
        //d.show();
    }

    private void deleteSelected(final ArrayList<Integer> dids){
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeScreenActivity.this);
        ProgressBar v=new ProgressBar(HomeScreenActivity.this, null,android.R.attr.progressBarStyleHorizontal);
        v.setIndeterminate(false);
        v.setMax(100);
        builder.setTitle("In Progress . . .");
        builder.setView(v);
        builder.setCancelable(false);
        AlertDialog d=builder.create();
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyDatabase db=MyDatabase.getInstance(HomeScreenActivity.this);
                final int[] a = {0};
                int each=dids.size()/100;
                for(Integer did:dids){
                    List<MyPicture> pics=db.myPicDao().getDocPics(did);
                    if(pics!=null){
                        for(MyPicture p:pics){
                            Uri uriOrig=Uri.parse(p.getOriginalUri());
                            Uri uriEdited=null;
                            if(p.getEditedUri()!=null) {
                                uriEdited = Uri.parse(p.getEditedUri());
                            }
                            try {
                                File f = new File(uriOrig.getPath());
                                if (f.exists()) {
                                    f.delete();
                                }
                                f=new File(uriEdited.getPath());
                                if(f.exists()){
                                    f.delete();
                                }
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }

                            db.myPicDao().deletePic(p);
                        }


                    }
                    MyDocument d=db.myDocumentDao().getDocumentWithId(did);
                    db.myDocumentDao().deleteDoc(d);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            a[0] +=each;
                            v.setProgress(a[0]);
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setProgress(100);
                        d.dismiss();
                        Toast.makeText(HomeScreenActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        myAdapter.notifyDataSetChanged();
                        myAdapter1.notifyDataSetChanged();
                    }
                });

            }
        }).start();
    }

    private void mergeSelected(final ArrayList<Integer> dids,final int targetDid){
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeScreenActivity.this);
        ProgressBar v=new ProgressBar(HomeScreenActivity.this, null,android.R.attr.progressBarStyleHorizontal);
        v.setIndeterminate(false);
        v.setMax(100);
        builder.setTitle("In Progress . . .");
        builder.setView(v);
        builder.setCancelable(false);
        AlertDialog d=builder.create();
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyDatabase db=MyDatabase.getInstance(HomeScreenActivity.this);
                MyDocument target=db.myDocumentDao().getDocumentWithId(targetDid);
                int targetSize=db.myPicDao().getCount(targetDid);
                int pos=targetSize+1;
                final int[] a = {0};
                int each=dids.size()/100;
                for(Integer did:dids){
                    MyDocument currDoc=db.myDocumentDao().getDocumentWithId(did);
                    List<MyPicture> pics=db.myPicDao().getDocPics(did);
                    if(pics!=null && currDoc.getDid()!=targetDid){
                        for(MyPicture p:pics){
                            p.setPosition(pos);
                            pos+=1;
                            p.setEditedName(p.getEditedName().replace(currDoc.getDname(),target.getDname()));
                            p.setDid(targetDid);
                        }
                        //all pics of curr is moved to target
                        db.myDocumentDao().deleteDoc(currDoc);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            a[0] +=each;
                            v.setProgress(a[0]);

                        }
                    });


                }

                //task Done
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setProgress(100);
                        d.dismiss();
                        Toast.makeText(HomeScreenActivity.this, "Documents Merged", Toast.LENGTH_SHORT).show();
                        myAdapter1.notifyDataSetChanged();
                        myAdapter.notifyDataSetChanged();

                    }
                });
            }
        }).start();
    }

    */
}

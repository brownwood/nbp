package globalsoft.com.testgps;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import globalsoft.com.dialoginterface.AlertDialogBuilder;
import globalsoft.com.reversegeocode.LocationLocator;

public class MtoQuestionnaireActivity extends AppCompatActivity {
    MTOExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    RadioButton radioButton;
    public int childID = 0;

    ArrayList<HashMap<String, String>> questans = new ArrayList<>();
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    ArrayList<HashMap<String, String>> optionHolder = new ArrayList<>();
    private HashMap<String, String> answersubquestion;
    public static ArrayList<HashMap<String, String>> subQuestOptionHolder = new ArrayList<>();

    private String uname, outletid;
    private String questionString;
    private static final String mtoURL = "http://www.nbappserver.com/nbpage/nbapi.php?optype=questions&outlettype=MTO";
    private static final String QUESTION = "question";
    private static final String QUESTION_ID = "questionid";
    private static final String SUB_QUEST_ID = "subquestionid";
    private static final String QUESTTION_PICTURE = "picture";

    //private static String HEADER1 = " Pricing (Price Tickets visibly evident on all NB products in the following locations:)".replace("\"", "");

    String question, qid, subquestid, picture;
    Bitmap bm;
    int initValue = 0;

    AlertDialogBuilder alert = new AlertDialogBuilder();
    Toolbar toolbar;
    LocationLocator locationLocator;
    SubQuestion subQuest;

    Button btnTakePic;
    String ba1;
    String mCurrentPhotoPath;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Error" + Thread.currentThread().getStackTrace()[2], paramThrowable.getLocalizedMessage());
            }
        });
        setContentView(R.layout.mto_questionnaire);
        Bundle bundle = getIntent().getExtras();
        uname = bundle.getString("USERNAME");
        outletid = bundle.getString("OUTLETID");
        //Log.e("USER_NAME", uname);
        locationLocator = new LocationLocator(MtoQuestionnaireActivity.this);
        locationLocator.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationLocator.updateGPS();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Trade Marketing Audit Soln");
        toolbar.setSubtitle("MTO Questionnaire");
        toolbar.setNavigationIcon(R.mipmap.nb_launcher);


        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        new MtoQuestions(MtoQuestionnaireActivity.this).execute();

        //listAdapter = new MTOExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        //expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " Expanded", Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                //Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " Collapsed", Toast.LENGTH_SHORT).show();
            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " : " + listDataChild.get(
                // listDataHeader.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
                questionString = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                if (loadPicture(childID).equals("1YES") || loadPicture(childID).equals("1NO")) {
                    loadDialogWithPicture(questionString);
                }
                //if (questionString.contains("Please provide picture") || questionString.contains("Please provide Picture")) {
                //Toast.makeText(MtoQuestionnaireActivity.this, "This question needs picture", Toast.LENGTH_LONG).show();
                //  loadDialogWithPicture(questionString);
                //}
                else {
                    loadDialog(questionString);
                }
                childID = (int) listAdapter.getChildId(groupPosition, childPosition);
                //listAdapter.getChildView(groupPosition,childID,false,v,parent).findViewById(R.id.lblMTOListItem).setBackgroundColor(0xFF697B5D);
                Log.e("NEWVALUE_OF_CHILDID", "" + childID);
                return false;
            }
        });

    }

    public String getQuestionId(int j) {
        String questID = "";
        for (int i = 0; i < questans.size(); i++) {
            questID = questans.get(j).get("questionid");
        }
        Log.e("QuestionID", questID);
        return questID;
    }

    public String getSubQuestionId(int j) {
        String subQuestID = "";
        for (int i = 0; i < questans.size(); i++) {
            subQuestID = questans.get(j).get("subquestionid");
        }
        Log.e("SubQuestionID", subQuestID);
        return subQuestID;
    }

    public String loadPicture(int j) {
        String picture = "";
        for (int i = 0; i < questans.size(); i++) {
            picture = questans.get(j).get("picture");
        }
        Log.e("SubQuestionID", picture);
        return picture;
    }


    private String loadDialog(String questionText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.questonnaireloader, null);
        TextView textv = (TextView) view.findViewById(R.id.textQuestion);
        textv.setText(questionText);
        // 2. Chain together various setter methods to set the dialog characteristics
        // builder.setView(inflater.inflate(R.layout.questonnaireloader, null).findViewById(R.id.textQuestion));

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioOption);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                int a = checkedId;
                System.out.println("Checked ID: " + a);
                if (a == 2131492997 && getSubQuestionId(childID).equals("1")) {
                    Intent loadSubQuestion = new Intent(MtoQuestionnaireActivity.this, SubQuestion.class);
                    loadSubQuestion.putExtra("QUEST_ID", getQuestionId(childID));
                    loadSubQuestion.putExtra("OUTLETID", outletid);
                    startActivityForResult(loadSubQuestion, 2);// Activity is started with requestCode 2

                    /*if (subQuestOptionHolder.size() != 0 || subQuest.subQuestOptHolder.size() !=0) {
                        subQuestOptionHolder.clear();
                    }*/
                }
            }
        });

        builder.setView(view);
        //.setMessage("Select an option")
        builder.setTitle("Questionnaire")
                .setIcon(R.mipmap.nb_launcher);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //dialog.dismiss();

            }
        })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(dialog));

        return questionText;
    }

    private String loadDialogWithPicture(String questionText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.questonnaire_picture_loader, null);
        TextView textv = (TextView) view.findViewById(R.id.textQuestion);
        textv.setText(questionText);
        btnTakePic = (Button) view.findViewById(R.id.btnTakePicture);
        mImageView = (ImageView) view.findViewById(R.id.imageView);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioOption);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                int a = checkedId;
                if (a == 2131492997 && getSubQuestionId(childID).equals("1")) {
                    Intent loadSubQuestion = new Intent(MtoQuestionnaireActivity.this, SubQuestion.class);
                    loadSubQuestion.putExtra("QUEST_ID", getQuestionId(childID));
                    loadSubQuestion.putExtra("OUTLETID", outletid);
                    startActivityForResult(loadSubQuestion, 2);// Activity is started with requestCode 2

                    /*if (subQuestOptionHolder.size() != 0 || subQuest.subQuestOptHolder.size() !=0) {
                        subQuestOptionHolder.clear();
                    }*/
                }
            }
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
        // 2. Chain together various setter methods to set the dialog characteristics
        // builder.setView(inflater.inflate(R.layout.questonnaireloader, null).findViewById(R.id.textQuestion));
        builder.setView(view);
        //.setMessage("Select an option")
        builder.setTitle("Questionnaire")
                .setIcon(R.mipmap.nb_launcher);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //dialog.dismiss();

            }
        })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(dialog));

        return questionText;
    }

    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        AlertDialogBuilder alert1 = new AlertDialogBuilder();

        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            // put your code here
            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioOption);
            int selectedRadio = radioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) dialog.findViewById(selectedRadio);
            TextView alert = (TextView) dialog.findViewById(R.id.textAlert);
            /*for (int i = 0; i < optionHolder.size(); i++) {
                if (optionHolder.get(i).get("id").equals(getQuestionId(childID))) {
                    alert.setText("You've already answered this question!\nPress cancel to answer a new question");
                    return;
                }
            }*/

            if (selectedRadio < 0) {
                alert.setText("Please select an option.");
            } else if (locationLocator.longitude == 0 || locationLocator.latitude == 0) {
                alert1.showAlertDialog(MtoQuestionnaireActivity.this, "Your location has not been captured\nYou can click on get location from the menu\nto manually capture this.", "Questionnaire", 0);
                //return;
            } else {
                String QUEST_ANS = "answer", QUEST_USERNAME = "username", QUEST_OUTLETID = "outletid", QUEST_PIC = "picture", QUEST_ID = "id", QUEST_GPS = "gps";
                //uploadImage();
                alert.setText("");
                Log.e("SlectedOption", radioButton.getText().toString());

                HashMap<String, String> optionMap = new HashMap<>();
                optionMap.put(QUEST_ID, getQuestionId(childID));
                optionMap.put(QUEST_ANS, radioButton.getText().toString());
                optionMap.put(QUEST_USERNAME, uname);
                optionMap.put(QUEST_PIC, ba1);
                optionMap.put(QUEST_OUTLETID, outletid);
                optionMap.put(QUEST_GPS, "Long: " + locationLocator.longitude + " Lat: " + locationLocator.latitude);
                optionHolder.add(optionMap);
                dialog.dismiss();
                ba1 = null;

                Log.e("OPTION_HOLDER", "" + optionHolder);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_questionnaire, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            alert.showAlertDialogWithOption(MtoQuestionnaireActivity.this, "Are you sure you want to exit?", "Confirm", R.mipmap.nb_launcher);
        } else if (id == R.id.action_save) {
            if (optionHolder.size() <= 0) {
                alert.showAlertDialog(MtoQuestionnaireActivity.this, "You haven't answer any of the questions!", "Nothing to save", 0);
            } else if (optionHolder.size() < questans.size()) {
                alert.showAlertDialog(MtoQuestionnaireActivity.this, "You must answer all questions!\nYou've answered " + optionHolder.size()
                        + " out of " + questans.size(), "Questionnaire", 0);
            } else {
                if (subQuest.subQuestOptHolder.size() > 0) {
                    //System.out.println("SizeOfSubQuestionAnswer: "+subQuest.subQuestOptionHolder.size());
                    //new SubmitSubQuestion().execute();
                    //new SubmitQuestion().execute();
                    postDataToServer();
                } else {
                    //new SubmitQuestion().execute();
                    postDataToServer();
                }

            }
            /*String question_answer_url = "";
            Toast.makeText(getApplicationContext(), "Questionnaire has been saved", Toast.LENGTH_LONG).show();
            Log.i("OPTION_HOLDER_ON_SAVE", "" + optionHolder);
            for (int i = 0; i < optionHolder.size(); i++) {
                question_answer_url = "http://populationassociationng.org/nbpage/getanswers.php?questionid=" + optionHolder.get(i).get("id") + "&answer=" + optionHolder.get(i).get("answer") + "&username=" + optionHolder.get(i).get("username") + "&picture=''&gps=''&outletid='' ";
                //question_answer_url = "http://populationassociationng.org/nbpage/nbapi.php?optype=answerquestion&questionid=" + optionHolder.get(i).get("id") + "&answer=" + optionHolder.get(i).get("answer") + "&username=" + optionHolder.get(i).get("username") + "&picture=''";
                Log.e("FinalSTRING", question_answer_url);
            }*/
        } else if (id == R.id.action_about) {
            String version = "Version " + BuildConfig.VERSION_NAME;
            alert.showAlertDialog(MtoQuestionnaireActivity.this, version, "About", R.mipmap.nb_launcher);
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadImage() {
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        //Log.e("The value of Image W n H","TargW"+targetW+" TargetH "+targetH+" PhotoW"+photoW+" PhotoH"+photoH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }

        bm = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.NO_WRAP);

        Log.e("BASE64VALUE==>", ba1);
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 100);
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setPic();
            uploadImage();
        }

        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            String QUEST_ID = "questionid", OUTLET_ID = "outletid", QUEST_SUBQUESID = "subquestionid", QUEST_ANS = "answer";
            answersubquestion = new HashMap<>();
            String id = data.getStringExtra("questID");
            String outletid = data.getStringExtra("outletid");
            String subqid = data.getStringExtra("subQuestID");
            String ans = data.getStringExtra("answer");

            answersubquestion.put(QUEST_ID, id);
            answersubquestion.put(OUTLET_ID, outletid);
            answersubquestion.put(QUEST_SUBQUESID, subqid);
            answersubquestion.put(QUEST_ANS, ans);

            subQuestOptionHolder.add(answersubquestion);
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        //Log.e("The value of Image W n H","TargW"+targetW+" TargetH "+targetH+" PhotoW"+photoW+" PhotoH"+photoH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.e("Getpath", "Cool" + mCurrentPhotoPath);
        return image;
    }


     /*This section is sending JSON data to the server*/

    private String formatDataAsJSON() {

        final JSONObject outletIdObject = new JSONObject();//This would be the root Object that would hold other objects and/array.

        try {
            outletIdObject.put("outletid", Integer.parseInt(outletid));
            outletIdObject.put("gps", "Long: " + locationLocator.longitude + " Lat: " + locationLocator.latitude);
            outletIdObject.put("username", uname);

            JSONArray answersArray = new JSONArray();
            ArrayList answersArrayList = new ArrayList();
            for (int i = 0; i < optionHolder.size(); i++) {
                //Create a JSON Object to hold answers for main questions.
                JSONObject answers = new JSONObject();
                //Log.e("MVOQuestionaire", "Inside for Loop..");
                answers.put("questionid", Integer.parseInt(optionHolder.get(i).get("id")));//optionHolder.get(i).get("id")  ContentValues values=new ContentValues();
                answers.put("answer", optionHolder.get(i).get("answer"));
                if (optionHolder.get(i).get("picture") != null) {
                    answers.put("picture", optionHolder.get(i).get("picture").toString());
                } else {
                    answers.put("picture", null);
                }
                answersArrayList.add(answers);

            }
            Log.e("MVOQuestionaire", "Array List size: " + answersArrayList.size());

            //Looping through the Array List and keep adding values to the answersArray object.
            for (int k = 0; k < answersArrayList.size(); k++) {
                answersArray.put(answersArrayList.get(k));
            }

            JSONArray subAnsArray = new JSONArray();
            ArrayList subAnsArrayList = new ArrayList();
            if (subQuest.subQuestOptHolder.size() > 0) {
                for (int j = 0; j < subQuest.subQuestOptHolder.size(); j++) {
                    //Create a JSON Object to hold answers for main sub questions.
                    JSONObject subanswers = new JSONObject();
                    subanswers.put("questionid", Integer.parseInt(subQuest.subQuestOptHolder.get(j).get("questionid")));
                    subanswers.put("subquestionid", Integer.parseInt(subQuest.subQuestOptHolder.get(j).get("subquestionid")));
                    subanswers.put("answer", subQuest.subQuestOptHolder.get(j).get("answer"));
                    subAnsArrayList.add(subanswers);
                }
            }

            if (subQuestOptionHolder.size() != 0) {
                for (int j = 0; j < subQuestOptionHolder.size(); j++) {
                    //Create a JSON Object to hold answers for main sub questions.
                    JSONObject subanswers = new JSONObject();
                    subanswers.put("questionid", Integer.parseInt(subQuestOptionHolder.get(j).get("questionid")));
                    subanswers.put("subquestionid", Integer.parseInt(subQuestOptionHolder.get(j).get("subquestionid")));
                    subanswers.put("answer", subQuestOptionHolder.get(j).get("answer"));
                    //Create a JSONArray to add answers for subquestion.
                    subAnsArrayList.add(subanswers);
                }

            }

            for (int k = 0; k < subAnsArrayList.size(); k++) {
                subAnsArray.put(subAnsArrayList.get(k));
            }

            //Add the arrays to the parent JSONObject.
            outletIdObject.put("answers", answersArray);
            outletIdObject.put("subanswers", subAnsArray);
            Log.e("MVOQuestionaire", outletIdObject.toString(1));

            return outletIdObject.toString();
        } catch (JSONException jsonex) {
            Log.d("Json Error", "Unable to format data to JSON");
        }
        //Log.e("MVOQuestActivity", "" + outletIdObject.toString());
        return "Unable to format JSON data";
    }

    private void postDataToServer() {

        final String json = formatDataAsJSON();

        new AsyncTask<Void, String, String>() {
            ProgressDialog pDialog = new ProgressDialog(MtoQuestionnaireActivity.this);

            @Override
            protected void onPreExecute() {
                pDialog.setMessage("Submitting Answer(s)\nPlease wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                if (pDialog.isShowing()) pDialog.dismiss();
                if (pDialog != null) pDialog = null;
                String statusMessage = "";
                Log.e("Response From Server", s);
                try {
                    JSONObject responseFromServer = new JSONObject(s);
                    statusMessage = responseFromServer.getString("status");
                    Log.e("Status ", statusMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusMessage.equalsIgnoreCase("Success")) {
                    alert.showAlertDialogForSavedQuestion(MtoQuestionnaireActivity.this, "Your question has been saved successfully!", "MVO Questionnaire", R.mipmap.nb_launcher);
                } else {
                    alert.showAlertDialogForSavedQuestion(MtoQuestionnaireActivity.this, "An error occurred while saving your answer(s)", "MVO Questionnaire", R.mipmap.nb_launcher);
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                pDialog.setProgress(Integer.parseInt(values[0]));
            }

            @Override
            protected String doInBackground(Void... voids) {
                initValue += 1;
                publishProgress("" + ((initValue * 100) / optionHolder.size()));
                Log.e("MVOJSONData", json);
                return getServerResponse(json);
            }
        }.execute();
    }

    private String getServerResponse(String json) {

        HttpPost post = new HttpPost("http://www.nbappserver.com/nbpage/getanswers.php");
        try {
            StringEntity entity = new StringEntity(json);
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            DefaultHttpClient client = new DefaultHttpClient();
            BasicResponseHandler handler = new BasicResponseHandler();
            String response = client.execute(post, handler);
            return response;
        } catch (UnsupportedEncodingException e) {
            Log.d("EntityError", e.toString());
        } catch (IOException e) {
            Log.d("EntityError", e.toString());
        }
        return "Unable to contact server.";
    }

    /*Ends here...*/

    private class MtoQuestions extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;

        Context context;

        public MtoQuestions(Activity listActivity) {
            context = listActivity;
            progressDialog = new ProgressDialog(context);
        }

        //public ExisitngOutletTask(OutletPage outletPage) {

        //}

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading Question(s)\nPlease wait...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String string) {
            //ListActivity listActivity = new ListActivity();
            if (progressDialog.isShowing()) progressDialog.dismiss();
            listAdapter = new MTOExpandableListAdapter(context, listDataHeader, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);

        }

        @Override
        protected String doInBackground(String... strings) {
            JSONParser jsonParser = new JSONParser();
            listDataHeader = new ArrayList<>();
            listDataChild = new HashMap<>();

            //get JSON string from URL
            JSONArray json = jsonParser.getMTOQuestions(mtoURL);


            try {
                //JSONObject c = json.getJSONObject(HEADER1);
                //JSONArray jsonArray = json.getJSONArray(0);

                for (int i = 0; i < json.length(); i++) {
                    //Log.e("INSIDE_MTO",""+c.getString(HEADER1));
                    //String question = c.getString(QUESTION);
                    JSONObject job = json.getJSONObject(i);
                    Iterator<?> keys = job.keys();
                    //int child = 0;
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        try {
                            //Log.e("INSIDE_MTO", "" + key);
                            listDataHeader.add(key);

                            JSONArray headers = job.getJSONArray(key);
                            List<String> questionList = new ArrayList<>();
                            for (int quest_in_headr = 0; quest_in_headr < headers.length(); quest_in_headr++) {
                                JSONObject jsonHeader1 = headers.getJSONObject(quest_in_headr);
                                question = jsonHeader1.getString(QUESTION);
                                qid = jsonHeader1.getString(QUESTION_ID);
                                subquestid = jsonHeader1.getString(SUB_QUEST_ID);
                                picture = jsonHeader1.getString(QUESTTION_PICTURE);

                                HashMap<String, String> map = new HashMap<>();
                                map.put(QUESTION_ID, qid.trim());
                                map.put(QUESTION, question);
                                map.put(SUB_QUEST_ID, subquestid);
                                map.put(QUESTTION_PICTURE, picture);
                                questans.add(map);
                                questionList.add(question.trim());
                                //Log.e("INSIDE_INNER_LOOP", question + " " + qid);
                                //Log.e("ADD_DATA", "" + question);
                            }
                            listDataChild.put(listDataHeader.get(i), questionList);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.e("VALUES_IN_QUESTANS", "" + questans);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }
    }

    private class SubmitQuestion extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog = new ProgressDialog(MtoQuestionnaireActivity.this);

        @Override
        protected String doInBackground(String... strings) {
            String result = "", st = "";
            String question_answer_url;
            long total = 0;
            //String gps = "41.25 and -120.9762";
            for (int i = 0; i < optionHolder.size(); i++) {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("questionid", optionHolder.get(i).get("id")));
                nameValuePairs.add(new BasicNameValuePair("answer", optionHolder.get(i).get("answer")));
                nameValuePairs.add(new BasicNameValuePair("username", optionHolder.get(i).get("username")));
                nameValuePairs.add(new BasicNameValuePair("gps", optionHolder.get(i).get("gps")));
                if (optionHolder.get(i).get("picture") != null) {
                    nameValuePairs.add(new BasicNameValuePair("picture", optionHolder.get(i).get("picture")));
                }
                nameValuePairs.add(new BasicNameValuePair("outletid", optionHolder.get(i).get("outletid")));
                question_answer_url = "http://www.nbappserver.com/nbpage/getanswers.php";
                //question_answer_url = "http://populationassociationng.org/nbpage/getanswers.php";

                try {
                    total += i;
                    publishProgress("" + (int) ((total * 100) / optionHolder.size()));

                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(question_answer_url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    StatusLine statusLine = response.getStatusLine();
                    st = EntityUtils.toString(response.getEntity());
                } catch (Exception e) {
                    Log.v("ERROR_OCCURRED", "Error in http connection " + e.toString());
                }
            }
            Log.e("VALUE_OF_ST", st);
            st = st.replace("\"", "");
            result = st.equalsIgnoreCase("Success") ? "Success" : "Something went wrong";

            return result;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Submitting Answer(s)\nPlease wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            if (progressDialog != null) progressDialog = null;
            Log.i("Response", s);
            if (s == "Success") {
                alert.showAlertDialogForSavedQuestion(MtoQuestionnaireActivity.this, "Your question has been saved successfully!", "MTO Questionnaire", R.mipmap.nb_launcher);
            }
            //Process.killProcess(Process.myPid());
        }
    }


    private class SubmitSubQuestion extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog = new ProgressDialog(MtoQuestionnaireActivity.this);

        @Override
        protected String doInBackground(String... strings) {
            String result = "", st = "";
            String sub_question_answer_url;

            if (subQuest.subQuestOptHolder.size() != 0) {

                for (int i = 0; i < subQuest.subQuestOptHolder.size(); i++) {
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("questionid", subQuest.subQuestOptHolder.get(i).get("questionid")));
                    nameValuePairs.add(new BasicNameValuePair("subquestionid", subQuest.subQuestOptHolder.get(i).get("subquestionid")));
                    nameValuePairs.add(new BasicNameValuePair("answer", subQuest.subQuestOptHolder.get(i).get("answer")));
                    nameValuePairs.add(new BasicNameValuePair("outletid", subQuest.subQuestOptHolder.get(i).get("outletid")));
                    sub_question_answer_url = "http://www.nbappserver.com/nbpage/subquestionanswers.php";
                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(sub_question_answer_url);
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpclient.execute(httppost);
                        StatusLine statusLine = response.getStatusLine();
                        st = EntityUtils.toString(response.getEntity());
                    } catch (Exception e) {
                        Log.v("ERROR_OCCURRED", "Error in http connection " + e.toString());
                    }
                }
            }

            for (int i = 0; i < subQuestOptionHolder.size(); i++) {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("questionid", subQuestOptionHolder.get(i).get("questionid")));
                nameValuePairs.add(new BasicNameValuePair("subquestionid", subQuestOptionHolder.get(i).get("subquestionid")));
                nameValuePairs.add(new BasicNameValuePair("answer", subQuestOptionHolder.get(i).get("answer")));
                nameValuePairs.add(new BasicNameValuePair("outletid", subQuestOptionHolder.get(i).get("outletid")));
                sub_question_answer_url = "http://www.nbappserver.com/nbpage/subquestionanswers.php";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(sub_question_answer_url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    StatusLine statusLine = response.getStatusLine();
                    st = EntityUtils.toString(response.getEntity());
                } catch (Exception e) {
                    Log.v("ERROR_OCCURRED", "Error in http connection " + e.toString());
                }
            }
            //Log.e("SubQuestion_Response", st);
            st = st.replace("\"", "");
            result = st.equalsIgnoreCase("Success") ? "Success" : st;

            return result;
        }

        @Override
        protected void onPreExecute() {
            //System.out.println("SizeOfSubQuestionAnswer: "+subQuest.subQuestOptionHolder.size());
            progressDialog.setMessage("Submitting Sub Question Answers\nPlease wait...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("Response From SubQuest", s);
            /*if (progressDialog.isShowing()) progressDialog.dismiss();
            Log.i("Response", s);
            if (s.equalsIgnoreCase("Success")) {
                alert.showAlertDialog(MvoQuestionnaireActivity.this, "Your question has been saved successfully!", "MVO Questionnaire", R.mipmap.nb_launcher);
                Process.killProcess(Process.myPid());
            }*/
        }
    }
}

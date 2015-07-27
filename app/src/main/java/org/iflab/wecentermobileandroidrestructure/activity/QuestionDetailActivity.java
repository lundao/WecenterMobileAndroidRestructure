package org.iflab.wecentermobileandroidrestructure.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apmem.tools.layouts.FlowLayout;
import org.iflab.wecentermobileandroidrestructure.R;
import org.iflab.wecentermobileandroidrestructure.adapter.AnswerAdapter;
import org.iflab.wecentermobileandroidrestructure.common.NetWork;
import org.iflab.wecentermobileandroidrestructure.http.AsyncHttpWecnter;
import org.iflab.wecentermobileandroidrestructure.http.RelativeUrl;
import org.iflab.wecentermobileandroidrestructure.model.article.ArticleInfo;
import org.iflab.wecentermobileandroidrestructure.model.personal.UserPersonal;
import org.iflab.wecentermobileandroidrestructure.model.question.AnswerInfo;
import org.iflab.wecentermobileandroidrestructure.model.question.QuestionInfo;
import org.iflab.wecentermobileandroidrestructure.model.question.QuestionTopics;
import org.iflab.wecentermobileandroidrestructure.tools.HawkControl;
import org.iflab.wecentermobileandroidrestructure.tools.ImageOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuestionDetailActivity extends BaseActivity implements View.OnClickListener{


    private Toolbar toolbar;
    ListView listView;
    View headerView;
    TextView userNameTextView;
    TextView contentTextView;
    TextView bookMarkTextView;
    TextView focusTextView;
    WebView contentWebView;
    ImageView userImageView;
    Button foucsBtn;
    FlowLayout topicFlowLayout;
    RelativeLayout addAnswerRel;
    RelativeLayout topRel;

    List<AnswerInfo> answersList = new ArrayList();
    List<QuestionTopics> questionsList;
    AnswerAdapter answerAdapter;
    int question_id;
    int has_foucs;
    int uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        uid = getIntent().getIntExtra("uid",-1);
        findViews();
        findHeaderView();
        setViews();
        setToolBars();
        loadData();
    }


    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView)findViewById(R.id.list_question_pic);
    }

    private void findHeaderView() {
        headerView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.question_header_view,null);
        userNameTextView =  (TextView)headerView.findViewById(R.id.txt_user_name);
        userImageView = (ImageView)headerView.findViewById(R.id.image_profile);
        contentTextView = (TextView)headerView.findViewById(R.id.txt_question_content);
        contentWebView = (WebView)headerView.findViewById(R.id.webv_question_content);
        topicFlowLayout = (FlowLayout)headerView.findViewById(R.id.flow_question_topic);
        bookMarkTextView = (TextView)headerView.findViewById(R.id.txt_bookmark);
        focusTextView = (TextView)headerView.findViewById(R.id.txt_focus);
        foucsBtn = (Button)headerView.findViewById(R.id.btn_foucs);
        addAnswerRel = (RelativeLayout)headerView.findViewById(R.id.rel_add_answer);
        topRel = (RelativeLayout)headerView.findViewById(R.id.rel_top);
    }

    private void setViews() {
        listView.addHeaderView(headerView);
        foucsBtn.setOnClickListener(this);
        topicFlowLayout.setOnClickListener(this);
        addAnswerRel.setOnClickListener(this);
        topRel.setOnClickListener(this);
    }

    private void setToolBars() {
        toolbar.setTitle("问题详情");//设置Toolbar标题
        toolbar.setTitleTextColor(Color.parseColor("#ffffff")); //设置标题颜色
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rel_add_answer:
                //到发布
                break;
            case R.id.btn_foucs:
                foucsQuestion();
                break;
            case R.id.rel_top:
                //到个人中心
                break;

            case R.id.flow_question_topic:
                Intent intent = new Intent(QuestionDetailActivity.this,TopicsActivity.class);
                ArrayList<String> topicsList = new ArrayList<>();
                for(QuestionTopics t:questionsList){
                    topicsList.add(t.getTopic_id()+"");
                }
                intent.putExtra("topics",topicsList);
                startActivity(intent);
                break;


        }
    }

    private void foucsQuestion(){
        AsyncHttpWecnter.loadData(QuestionDetailActivity.this, RelativeUrl.QUESTION_FOUCS, setFoucsParams(), AsyncHttpWecnter.Request.Get, new NetWork() {
            @Override
            public void parseJson(JSONObject response) {
                // update button UI
                if(has_foucs == 0){
                    has_foucs = 1;
                }else if(has_foucs == 1){
                    has_foucs = 0;
                }
                updateFoucsBtnUI(has_foucs);
            }
        });
    }


    private void loadData() {
        AsyncHttpWecnter.loadData(QuestionDetailActivity.this, RelativeUrl.QUESTION_INFO, setParams(), AsyncHttpWecnter.Request.Get, new NetWork() {
            QuestionInfo questionInfo;
            AnswerInfo answerInfo;

            @Override
            public void parseJson(JSONObject response) {
                Gson gson = new Gson();
                try {
                    //questionInfo
                    questionInfo = gson.fromJson(response.getString("question_info"), QuestionInfo.class);
                    Log.v("questionInfo",questionInfo.toString());

                    //answer
                    JSONObject answersObj = new JSONObject(response.getString("answers"));
                    Iterator<String> iterator = answersObj.keys();
                    while (iterator.hasNext()) {
                        answerInfo = new AnswerInfo();
                        answerInfo = gson.fromJson(answersObj.getString(iterator.next()), AnswerInfo.class);
                        answersList.add(answerInfo);
                        Log.v("answerInfo", answerInfo.toString());
                    }
                    answerAdapter = new AnswerAdapter(QuestionDetailActivity.this,answersList,questionInfo.getQuestion_content());
                    listView.setAdapter(answerAdapter);

                    //QuestionTopics
                    questionsList = gson.fromJson(response.getString("question_topics"),
                            new TypeToken<ArrayList<QuestionTopics>>() {
                            }.getType());
//                    Log.v("QuestionTopics",questionsList.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                question_id = questionInfo.getQuestion_id();
                has_foucs = questionInfo.getHas_focus();
                updateFoucsBtnUI(has_foucs);

                contentWebView.loadDataWithBaseURL("about:blank", questionInfo.getQuestion_detail(), "text/html", "utf-8", null);
                contentWebView.setBackgroundColor(getResources().getColor(R.color.bg_color_grey));
                contentTextView.setText(questionInfo.getQuestion_content());
                bookMarkTextView.setText(questionInfo.getFocus_count() + "");
                focusTextView.setText(questionInfo.getHas_focus()+"");

                for(QuestionTopics q:questionsList){
                    addFlowTopics(q.getTopic_title());
                }

            }
        });

        // load user data
        AsyncHttpWecnter.loadData(QuestionDetailActivity.this, RelativeUrl.USER_INFO,setUserParams(), AsyncHttpWecnter.Request.Get, new NetWork() {
            @Override
            public void parseJson(JSONObject response) {
                UserPersonal user = new UserPersonal(response);
                HawkControl.saveUserCount(user);

                userNameTextView.setText(user.getUser_name());
                ImageLoader.getInstance().displayImage(user.getAvatar_file(), userImageView, ImageOptions.optionsImage);
            }
        });
    }

    private void updateFoucsBtnUI(int hasFoucs){
        switch (hasFoucs){
            case 0:
                foucsBtn.setText("关注");
                break;
            case 1:
                foucsBtn.setText("已关注");
                break;
        }
    }

    private void addFlowTopics(String toipcString){
        TextView button = new TextView(QuestionDetailActivity.this);
        button.setText(toipcString);
        button.setBackground(getResources().getDrawable(R.drawable.public_topic));
        button.setTextColor(Color.WHITE);
        button.setPadding(10, 10, 10, 10);
        topicFlowLayout.addView(button);
    }

    private RequestParams setParams() {
        RequestParams params = new RequestParams();
//        params.put("id",articleID);
        params.put("id", 2);
        return params;
    }

    private RequestParams setFoucsParams() {
        RequestParams params = new RequestParams();
//        params.put("id",articleID);
        params.put("question_id", question_id);
        return params;
    }

    private RequestParams setUserParams(){
        RequestParams params = new RequestParams();
        params.put("uid", uid);
        return params;
    }

}
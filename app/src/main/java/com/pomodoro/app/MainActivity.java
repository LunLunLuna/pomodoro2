package com.pomodoro.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class MainActivity extends AppCompatActivity {

    // 로그 태그 정의 (디버깅용)
    private static final String TAG = "MainActivity";

    // WebView 객체 선언
    private WebView mWebView;

    // 전면 광고 객체 선언
    private InterstitialAd mInterstitialAd;

    // 마지막으로 뒤로가기 버튼이 눌린 시간을 저장하는 변수
    long lastTimeBackPressed = 0L;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 설정
        mWebView = findViewById(R.id.webView); // 레이아웃에서 WebView 객체를 가져옴

        // WebView 설정
        mWebView.getSettings().setJavaScriptEnabled(true); // JavaScript 기능 활성화
        mWebView.setWebChromeClient(new WebChromeClient()); // WebChromeClient 설정 (알림, 팝업, 진행률 표시 처리)
        mWebView.setWebViewClient(new WebViewClient()); // WebViewClient 설정 (링크 클릭 시 외부 브라우저가 아닌 WebView에서 열리도록 설정)

        // 클릭 시 사운드 효과 추가 (필요하지 않다면 제거 가능)
        mWebView.playSoundEffect(SoundEffectConstants.CLICK);

        // DOM Storage 활성화 (HTML5 로컬 저장소 지원)
        mWebView.getSettings().setDomStorageEnabled(true);

        // 미디어 자동 재생 설정 (사용자 제스처 없이 자동 재생 허용)
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        WebSettings webSettings = mWebView.getSettings(); // WebSettings 객체 가져오기

        // 페이지 화면 맞춤 설정
        webSettings.setLoadWithOverviewMode(true); // 메타태그로 화면 사이즈 조정 허용
        webSettings.setUseWideViewPort(true); // WebView의 콘텐츠가 화면 크기에 맞게 조정되도록 설정

        // 줌 설정
        webSettings.setSupportZoom(false); // 줌 기능 비활성화
        webSettings.setBuiltInZoomControls(false); // 줌 컨트롤 비활성화

        // WebView에 표시할 웹사이트 로드
        mWebView.loadUrl("https://devmango1128.github.io/examClassApp/");

        // 광고 요청 객체 생성
        AdRequest adRequest = new AdRequest.Builder().build();

        // 전면 광고 로드 및 콜백 설정
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd; // 광고 객체 저장
                        mInterstitialAd.show(MainActivity.this); // 광고 표시
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        mInterstitialAd = null;  // 광고 로드 실패 시 객체 초기화
                        Log.d(TAG, "광고 로드 실패: " + adError.getMessage()); // 오류 메시지 로그 출력

                    }
                });

        mWebView.setWebChromeClient(new WebChromeClient() {
            //alert custom, url 나오는거 없애기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                        setMessage(message).
                        setPositiveButton("OK", (dialog1, which) -> {}).create();
                dialog.show();
                result.confirm();
                return true;
            }

            //confirm custom, url 나오는거 없애기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog12, which) -> {
                            result.confirm(); // OK 클릭 시 confirm 처리
                        })
                        .setNegativeButton("Cancel", (dialog13, which) -> {
                            result.cancel(); // Cancel 클릭 시 cancel 처리
                        })
                        .create();
                dialog.show();
                return true;
            }
        });

        // URL 로드 처리 설정 (mailto 링크 처리)
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                if (request != null && request.getUrl() != null) {
                    String url = request.getUrl().toString();
                    if (url.startsWith("mailto:")) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                        try {
                            view.getContext().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(view.getContext(), "No email app found", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // 뒤로가기 버튼 처리
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // WebView가 뒤로 갈 수 있는지 확인
                if (mWebView.canGoBack()) { // WebView에서 뒤로 갈 수 있는지 확인
                    mWebView.goBack(); // 뒤로가기
                } else {

                    // 앱이 업데이트되었는지 확인하고 리뷰 요청 처리
                    showReviewDialog();

                    // 1.5초 내에 두 번 눌렀는지 확인
                    if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
                        finish(); // 액티비티 종료
                    } else {
                        lastTimeBackPressed = System.currentTimeMillis(); // 마지막 누른 시간 업데이트
                        Toast.makeText(MainActivity.this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(MainActivity.this, callback);
    }

    // 리뷰 요청 다이얼로그 표시
    private void showReviewDialog() {
        new AlertDialog.Builder(this)
                .setTitle("pomodoro")
                .setMessage("앱이 마음에 드셨다면 리뷰를 작성해 주세요! :)")
                .setPositiveButton("예", (dialog, which) -> initReviewManager())  // 리뷰 요청 시작
                .setNegativeButton("아니오", null)// 아무 작업도 하지 않음
                .setIcon(android.R.drawable.ic_dialog_info) // 아이콘 설정
                .show();
    }

    // In-App 리뷰 요청 초기화
    private void initReviewManager() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow(); // 리뷰 플로우 요청
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo); // 리뷰 플로우 시작
                flow.addOnCompleteListener(flowTask -> {
                    // 리뷰 플로우가 완료되었습니다.
                    // Google Play 스토어 애플리케이션으로 이동
                    openGooglePlayForReview();
                });
            } else {
                // In-App 리뷰 요청 실패 시 Google Play 스토어로 이동
                openGooglePlayForReview();
            }
        });
    }

    // Google Play 스토어 리뷰 페이지 열기
    private void openGooglePlayForReview() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.pomodoro.devmango1128")); // 앱 ID에 해당하는 Play Store 페이지
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 작업으로 실행
        startActivity(intent);
    }

    // 메모리 누수 방지를 위해 WebView 자원 해제
    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.clearCache(true); // 캐시 데이터 삭제
            mWebView.clearHistory(); // 브라우저 히스토리 삭제
            mWebView.destroy(); // WebView 파괴
        }
        super.onDestroy();
    }
}
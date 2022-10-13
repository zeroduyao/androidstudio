package kale.sharelogin.weibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import kale.sharelogin.EventHandlerActivity;
import kale.sharelogin.IPlatform;
import kale.sharelogin.LoginListener;
import kale.sharelogin.ShareListener;
import kale.sharelogin.ShareLoginLib;
import kale.sharelogin.content.ShareContent;
import kale.sharelogin.content.ShareContentType;

/**
 * @author Kale
 * @date 2018/9/10
 */
public class WeiBoPlatform implements IPlatform {

    public static final String KEY_APP_KEY = "weibo_key_app_key";

    public static final String KEY_REDIRECT_URL = "key_redirect_url";

    public static final String KEY_SCOPE = "key_scope";

    // ---------------------------------------------------------------

    public static final String LOGIN = "weibo_login";

    public static final String TIMELINE = "weibo_timeline";

    public static final String STORY = "weibo_story";

    private WbShareCallback shareCallback;

    private SsoHandler ssoHandler;

    @Override
    public String[] getSupportedTypes() {
        return new String[]{LOGIN, TIMELINE, STORY};
    }

    @Override
    public boolean isAppInstalled(@NonNull Context context) {
        return WbSdk.isWbInstall(context);
    }

    @Override
    public void checkEnvironment(Context context, @NonNull String type, int shareContentType) {
        // 1. 检测是否初始化
        if (TextUtils.isEmpty(ShareLoginLib.getValue(KEY_APP_KEY))) {
            throw new IllegalArgumentException("微博的appId未被初始化，当前为空");
        }

        // 2. 进行进行初始化操作
        try {
            WbSdk.checkInit();
        } catch (RuntimeException e) {
            // 如果没有init，则init一次，之后都不用再做任何初始化操作
            AuthInfo authInfo = new AuthInfo(context, ShareLoginLib.getValue(KEY_APP_KEY),
                    ShareLoginLib.getValue(KEY_REDIRECT_URL), ShareLoginLib.getValue(KEY_SCOPE));
            WbSdk.install(context, authInfo);
        }

        // 3. 微博不支持分享音乐
        if (shareContentType == ShareContentType.MUSIC) {
            throw new UnsupportedOperationException("目前不能向微博分享音乐");
        }

        // 4. 必须安装微博才能分享到微博故事
        if (type.equals(STORY)) {
            if (!isAppInstalled(context)) {
                throw new UnsupportedOperationException("必须安装微博后才能分享微博故事");
            }

            if (shareContentType != ShareContentType.PIC) {
                throw new UnsupportedOperationException("微博故事只能分享单个图片和视频");
            }
        }
    }

    @Override
    public void doLogin(@NonNull Activity activity, @NonNull LoginListener listener) {
        ssoHandler = new SsoHandler(activity);
        ssoHandler.authorize(new WbAuthListener() {
            @Override
            public void onSuccess(Oauth2AccessToken token) {
                LoginHelper.parseLoginResp(activity, token, listener);
            }

            @Override
            public void cancel() {
                listener.onCancel();
                // 网页登录时无法正常关闭activity，这里必须手动调用finish()
                activity.finish();
            }

            @Override
            public void onFailure(WbConnectErrorMessage err) {
                listener.onError(err.getErrorMessage());
            }
        });
    }

    @Override
    public void doShare(Activity activity, String shareType, @NonNull ShareContent shareContent, @NonNull ShareListener listener) {
        shareCallback = new WbShareCallback() {
            @Override
            public void onWbShareSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onWbShareCancel() {
                listener.onCancel();
            }

            @Override
            public void onWbShareFail() {
                listener.onError("未知异常");
            }
        };

        WbShareHandler shareHandler = new WbShareHandler(activity);
        shareHandler.registerApp();

        if (shareType.equals(TIMELINE)) {
            shareHandler.shareMessage(ShareHelper.shareMessage(shareContent), false);
        } else if (shareType.equals(STORY)) {
            shareHandler.shareToStory(ShareHelper.storyMessage(shareContent));
        }
    }

    @Override
    public void onResponse(@NonNull Activity activity, @Nullable Intent data) {
        if (shareCallback != null) {
            // 分享
            if (data == null) {
                shareCallback.onWbShareFail();
            } else {
                // 处理保存到草稿箱的逻辑，判断是否有某个值，如果没有则说明是保存到草稿了
                if (data.getIntExtra("_weibo_resp_errcode", -31) == -31) {
                    shareCallback.onWbShareCancel();
                } else {
                    new WbShareHandler(activity).doResultIntent(data, shareCallback);
                }
            }
        } else {
            // 登录
            if (data == null) {
                ssoHandler.authorizeCallBack(32973, 0, null);
            } else {
                int requestCode = data.getIntExtra(EventHandlerActivity.KEY_REQUEST_CODE, -1);
                int resultCode = data.getIntExtra(EventHandlerActivity.KEY_RESULT_CODE, -1);
                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void getUserInfo(Context context, String accessToken, String uid, LoginListener listener) {
        LoginHelper.getUserInfo(context, accessToken, uid, listener);
    }

}
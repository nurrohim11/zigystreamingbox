package id.net.gmedia.zigistreamingbox.utils;

import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    public abstract void onShow();
    public abstract void requestFocus();
    public abstract void clearFocus();
    public  boolean isFocus(){return getView().hasFocus();};
    public abstract boolean onKeyDown(int keyCode, KeyEvent event);

}

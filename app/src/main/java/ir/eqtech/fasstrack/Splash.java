package ir.eqtech.fasstrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private ImageView caravel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        caravel = (ImageView) findViewById(R.id.splash_caravel_img);
        TranslateAnimation animation = new TranslateAnimation(0.0f, 900.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(4000);
        caravel.startAnimation(animation);



        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent gotoMain = new Intent(Splash.this , MainActivity.class);
                    startActivity(gotoMain);
                    finish();
                }
            }, 4000);


    }
}

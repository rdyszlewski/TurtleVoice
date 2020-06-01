package com.example.annac.turtleai;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.musicg.wave.Wave;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.annac.turtleai.R.color.colorRed;
import static com.example.annac.turtleai.Spectrogram.getSpectrogram;

import org.tensorflow.lite.Interpreter;

public class TurtleActivity extends AppCompatActivity {

    //obiekty z Acivity
    private TextView startLabel;
    private ImageView turtle;

    //wspolrzedne bohatera
    private int turtleY;
    private int turtleX;

    //reakcja na zdarzenia
    private Handler handler = new Handler();
    private Timer timer = new Timer();

    //sprawdz kiedy start with movement
    private boolean action_flag = false;
    private boolean start_flag = false;

    //Rozmiar ekranu i zolwika - pilnujemy zeby nie wychodzic poza
    private int frameHeight;
    private int turtleSize;

    protected Interpreter model;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turtle);

        startLabel = (TextView) findViewById(R.id.startLabel);
        turtle = (ImageView) findViewById(R.id.turtle);

        turtleY=500; // |
        turtleX=0;// --

        try {
            model = new Interpreter(loadModelFile(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] labelProbArray = new String[]{"go", "left", "right"};

        String inpData = "380:256 bed:0.775051 0.797632 0.808684 0.786000 0.782948 0.796891 0.766894 0.782579 0.796625 0.786670 0.764522 0.788482 0.767416 0.794011 0.793483 0.789613 0.796821 0.791620 0.773628 0.763393 0.771362 0.774247 0.773557 0.756208 0.749788 0.765875 0.779694 0.767050 0.756096 0.725428 0.755168 0.724514 0.770650 0.782658 0.768879 0.760494 0.762699 0.759112 0.776090 0.784395 0.795320 0.797478 0.783026 0.767329 0.786098 0.779390 0.780512 0.763100 0.755824 0.765713 0.769452 0.756968 0.756281 0.757859 0.753058 0.777633 0.778241 0.765819 0.746215 0.752366 0.761764 0.764709 0.751005 0.758093 0.779669 0.778750 0.728010 0.730189 0.751886 0.764403 0.774063 0.771370 0.767753 0.756773 0.757883 0.772618 0.770060 0.777040 0.779914 0.775220 0.773730 0.767927 0.763241 0.784888 0.789400 0.795669 0.791968 0.775777 0.786970 0.794029 0.797965 0.803134 0.803438 0.804601 0.804615 0.809515 0.813088 0.812578 0.811605 0.807196 0.803968 0.805475 0.809319 0.809476 0.807135 0.807371 0.810785 0.809949 0.809333 0.810563 0.811151 0.809400 0.813581 0.812936 0.805851 0.803929 0.814203 0.817982 0.820278 0.822336 0.817925 0.809203 0.805726 0.811769 0.815772 0.811044 0.799427 0.787714 0.761976 0.782471 0.801610 0.807476 0.810282 0.814054 0.813817 0.812741 0.808767 0.802617 0.804106 0.798993 0.790931 0.804330 0.806582 0.800401 0.795319 0.785526 0.772231 0.792066 0.800217 0.802842 0.805914 0.806120 0.803654 0.802818 0.800380 0.794914 0.801945 0.807256 0.806085 0.801742 0.794358 0.784999 0.778040 0.771789 0.732078 0.729602 0.747618 0.721475 0.747200 0.767606 0.774732 0.782408 0.786773 0.787965 0.788472 0.789403 0.792624 0.797097 0.799099 0.798663 0.794648 0.792121 0.796371 0.798786 0.797813 0.792330 0.780984 0.770806 0.785637 0.791395 0.791180 0.787484 0.775447 0.767537 0.739033 0.759153 0.768139 0.753392 0.735472 0.762125 0.766132 0.738699 0.761651 0.769587 0.769051 0.776728 0.774314 0.759396 0.744298 0.767530 0.772664 0.773646 0.768402 0.761775 0.759738 0.763742 0.767791 0.775049 0.770044 0.757911 0.762446 0.767787 0.778596 0.766170 0.749480 0.748614 0.741566 0.767802 0.762571 0.755804 0.763913 0.764097 0.763761 0.760477 0.770918 0.771837 0.744645 0.766920 0.767296 0.757974 0.781646 0.782523 0.763424 0.759998 0.770563 0.768310 0.742852 0.758482 0.759574 0.762909 0.769941 0.775031 0.774612 0.771895 0.768157 0.778015 0.775756 0.797295 0.809560 0.789159 0.785921 0.799347 0.768313 0.783644 0.796409 0.787685 0.776425 0.790250 0.755100 0.792959 0.792775 0.792259 0.795590 0.786566 0.768839 0.758145 0.765795 0.783922 0.783747 0.764089 0.777363 0.778437 0.780943 0.769491 0.780079 0.773172 0.766429 0.773856 0.751913 0.767812 0.775657 0.782582 0.780707 0.786048 0.758321 0.773822 0.760901 0.766989 0.777710 0.779902 0.769726 0.786991 0.796833 0.793781 0.784771 0.769057 0.757749 0.737142 0.749459 0.774306 0.774769 0.752257 0.758746 0.759008 0.740499 0.773935 0.786143 0.772373 0.772609 0.768580 0.747264 0.732318 0.764365 0.777558 0.780092 0.770240 0.761473 0.760560 0.747889 0.760063 0.767022 0.777854 0.784073 0.773437 0.776195 0.781004 0.777430 0.780256 0.782241 0.771736 0.775878 0.783768 0.778787 0.773122 0.780402 0.774856 0.785570 0.799994 0.805791 0.800604 0.797291 0.808791 0.812717 0.807317 0.805339 0.805403 0.805074 0.802781 0.806050 0.809243 0.806982 0.810418 0.812238 0.810070 0.807600 0.810544 0.811512 0.807170 0.812746 0.814820 0.809882 0.806165 0.813781 0.818768 0.822031 0.822266 0.816978 0.811471 0.808951 0.812389 0.816626 0.814052 0.805421 0.795246 0.778362 0.760691 0.798319 0.806303 0.806987 0.810325 0.808220 0.810812 0.808849 0.803082 0.806989 0.802375 0.792389 0.804081 0.808979 0.805575 0.803595 0.796423 0.778091 0.790050 0.798597 0.800649 0.802345 0.803024 0.800964 0.797669 0.794748 0.783614 0.798881 0.806447 0.806027 0.800094 0.792530 0.781440 0.774178 0.773095 0.749946 0.742297 0.741856 0.711940 0.750648 0.767213 0.773060 0.779150 0.784995 0.789083 0.792374 0.790533 0.792355 0.796337 0.796266 0.796495 0.792643 0.789677 0.795067 0.796525 0.793353 0.789940 0.783653 0.771203 0.784834 0.788389 0.789163 0.787647 0.777170 0.767844 0.752967 0.766783 0.768134 0.752006 0.735548 0.761340 0.765451 0.729144 0.762847 0.766525 0.765732 0.777143 0.776189 0.765958 0.757099 0.764958 0.767253 0.774395 0.768716 0.755051 0.755003 0.759367 0.766334 0.778602 0.773677 0.746510 0.762101 0.767461 0.778570 0.765793 0.754449 0.752652 0.738840 0.766962 0.763858 0.757814 0.768671 0.772457 0.770006 0.756802 0.766193 0.768596 0.731498 0.762737 0.761013 0.752984 0.781567 0.781317 0.761062 0.762414 0.771908 0.765903 0.738392 0.759457 0.762004 0.764486 0.771246 0.774656 0.771844 0.770586 0.767633 0.777559 0.779324 0.800023 0.810538 0.790664 0.789410 0.801151 0.768251 0.785636 0.797350 0.791491 0.781675 0.791639 0.745903 0.794324 0.790818 0.793735 0.794453 0.786959 0.773298 0.760145 0.767610 0.769575 0.771752 0.755352 0.758626 0.769199 0.777926 0.763503 0.753668 0.744086 0.750993 0.728564 0.765676 0.777525 0.761822 0.757267 0.763463 0.752456 0.778439 0.781373 0.793726 0.797384 0.782022 0.756230 0.787108 0.782794 0.780083 0.755275 0.749645 0.763678 0.764786 0.752298 0.759302 0.758949 0.758943 0.780844 0.781439 0.769593 0.753449 0.758127 0.754160 0.764343 0.746990 0.755994 0.778339 0.778114 0.739371 0.742801 0.752661 0.759578 0.773656 0.767132 0.764980 0.754280 0.753885 0.772891 0.763189 0.774591 0.779005 0.773994 0.768661 0.767408 0.762417 0.786071 0.790154 0.795695 0.791714 0.765335 0.785597 0.791675 0.794687 0.802927 0.800993 0.802566 0.800205 0.807893 0.812292 0.810204 0.809346 0.803821 0.800703 0.802349 0.807415 0.806909 0.803995 0.804127 0.808864 0.807090 0.806197 0.807364 0.809311 0.805260 0.811673 0.811063 0.802272 0.799177 0.813076 0.816373 0.818288 0.821622 0.816246 0.805608 0.801152 0.809592 0.815127 0.809470 0.798404 0.787617 0.771147 0.785176 0.801837 0.806753 0.808652 0.813119 0.811029 0.811522 0.807838 0.798701 0.802879 0.797234 0.783158 0.803108 0.806652 0.797041 0.792438 0.782985 0.760313 0.791085 0.798855 0.799973 0.803503 0.804860 0.801384 0.800098 0.798392 0.789263 0.799078 0.806511 0.805385 0.799884 0.791680 0.781845 0.773834 0.769747 0.726961 0.727098 0.749498 0.718070 0.748515 0.765778 0.771095 0.779525 0.784010 0.784808 0.785735 0.785932 0.790436 0.795053 0.796719 0.796349 0.791498 0.789006 0.794004 0.796467 0.794910 0.791030 0.778727 0.760729 0.784785 0.789321 0.788935 0.785833 0.773617 0.770290 0.753970 0.763608 0.767237 0.751615 0.733838 0.765265 0.767130 0.713488 0.761540 0.768294 0.762361 0.775536 0.775703 0.759432 0.736166 0.764666 0.767992 0.771322 0.764175 0.758895 0.755738 0.760496 0.762671 0.777303 0.769395 0.750102 0.761648 0.764768 0.779974 0.765437 0.748836 0.749721 0.743846 0.770112 0.761863 0.752306 0.766251 0.768455 0.767142 0.766780 0.772948 0.771589 0.722880 0.766551 0.768973 0.746017 0.782362 0.781838 0.761148 0.758565 0.771237 0.767185 0.721047 0.754615 0.759257 0.759419 0.769969 0.772585 0.767983 0.768351 0.765398 0.776236 0.784497 0.801824 0.810382 0.793116 0.792787 0.803434 0.771754 0.786855 0.796374 0.791644 0.788141 0.794072 0.735519 0.793168 0.788741 0.795093 0.792931 0.781024 0.767963 0.755348 0.758563 0.780937 0.781318 0.758875 0.778903 0.777786 0.778380 0.755867 0.776817 0.772247 0.759930 0.768199 0.696694 0.772179 0.775666 0.778782 0.773441 0.786368 0.746089 0.773749 0.755123 0.766103 0.774773 0.780391 0.758242 0.783265 0.793907 0.793352 0.784385 0.773843 0.762301 0.742111 0.740513 0.772275 0.775654 0.744364 0.757267 0.754744 0.746713 0.775843 0.785636 0.763982 0.772340 0.769566 0.752557 0.743119 0.762646 0.773668 0.775586 0.765061 0.759452 0.755495 0.739699 0.759309 0.764207 0.776475 0.782772 0.771137 0.772549 0.776531 0.773355 0.776465 0.781498 0.767123 0.773086 0.782155 0.774628 0.765384 0.777825 0.768420 0.783097 0.797236 0.804085 0.797090 0.791360 0.806521 0.810849 0.802950 0.801547 0.801773 0.801131 0.798023 0.802146 0.805478 0.801832 0.806961 0.808944 0.806554 0.802894 0.807134 0.808096 0.800849 0.810052 0.812238 0.806022 0.799721 0.810482 0.815330 0.818963 0.820250 0.813869 0.807290 0.803621 0.808773 0.815050 0.811689 0.802570 0.792876 0.778170 0.767610 0.797811 0.804684 0.803422 0.808847 0.803581 0.808149 0.806479 0.797497 0.805750 0.799899";
        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("0b09edd3_nohash_0.wav");
        } catch (IOException e) {
            e.printStackTrace();
        }
        double[][] specData = getSpectrogram(inputStream, 1024, 30);
        String[] splited = inpData.split(" ");

        model.run(specData, splited);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetManager manager = activity.getAssets();
        AssetFileDescriptor fileDescriptor = manager.openFd("converted_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    public void changePos()
    {
        //move turtle
        if( action_flag == true){
            turtleY -=20;
        }else{
            turtleY +=20;
        }
        //sprawdz czy nie wyjdziemy za dalekko
        if (turtleY < 0)
            turtleY = 0;

        if (turtleY > frameHeight - turtleSize)
            turtleY = frameHeight - turtleSize;

        //TODO sprawdz czt nie wyszedl ponad gorne linie

        turtle.setX(turtleX);
        turtle.setY(turtleY);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {

        if (start_flag == false) {
            //first tap on screen from user
            start_flag = true;
            //check user mobile screen size
            FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
            frameHeight = frame.getHeight();

            turtleY= (int)turtle.getY();
            turtleSize = turtle.getHeight();

            startLabel.setVisibility(View.GONE);
            //zaczynam petle ze zmiana pozycji
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            }, 0 ,20);

        }
        else{
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                action_flag = true;
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                action_flag = false;
        }



        /*turtle.setY(turtleY);
        przenosze do time eventu
        turtle.setX(turtleX);*/
        return true;
    }
    /*TODO rekacje na YES, NO, UP, DOWN, LEFT, RIGHT, ON, OFF, STOP, GO, UNKNOWN
     *down up right left X,Y zolwik
     *UNKNOWN - screen flash RED
     *start - stop - zatrzymuje all? znika i pojawia sie zolwik
     *zostaje nieobsluzone yes,np,on,off,go
     *go* - sam sobie losuje liczby i lazi gdzie chce
     * ponisze funkcje sa przygotowane dla modelu, aktualnie umieszczam je na buttonach, celem przetestowania
     */
     public void commandUnknown(View view){
        // final FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
         //frame.setBackgroundColor(Color.RED);
         final ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.tlo);
         constraintLayout.setBackgroundColor(Color.RED);
         //przwyroc default kolor po jakims czasie
         Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 constraintLayout.setBackgroundColor(Color.WHITE);
             }
         }, 500);


     }

}

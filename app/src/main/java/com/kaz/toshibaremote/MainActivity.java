package com.kaz.toshibaremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
  private ConsumerIrManager transmitter;
  private SharedPreferences prefs;
  private TextView status;
  private final Map<String,String> builtins = new LinkedHashMap<>();
  private final int navy = 0xff101827, surface = 0xff19263b, blue = 0xff4b9cff;

  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    transmitter=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
    prefs=getSharedPreferences("toshiba_ir",MODE_PRIVATE);
    loadToshibaNEC1();
    getWindow().setStatusBarColor(navy);
    getWindow().setNavigationBarColor(navy);
    ScrollView scroll=new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(navy);
    LinearLayout page=new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setPadding(dp(18),dp(24),dp(18),dp(28));
    scroll.addView(page);
    TextView title=label("KAZ  TOSHIBA",27,true,Color.WHITE);
    page.addView(title);
    TextView sub=label("REMOTE TV  •  Xiaomi 11T  •  Hồng ngoại",13,false,0xffaac0dc);
    page.addView(sub);
    status=label("",13,false,0xff86e2bb);
    status.setPadding(0,dp(18),0,dp(18));
    page.addView(status);
    boolean ok=transmitter!=null&&transmitter.hasIrEmitter();
    status.setText(ok?"● Đã nhận diện bộ phát hồng ngoại":"● Thiết bị không hỗ trợ phát hồng ngoại");
    if(!ok)status.setTextColor(0xffffbe78);

    TextView info=label("BỘ MÃ: Toshiba NEC1 (40BF) • thử trước với POWER",12,false,0xffaac0dc);
    info.setPadding(0,0,0,dp(12));page.addView(info);
    addRow(page,new String[]{"POWER","MUTE"},true);
    addRow(page,new String[]{"SOURCE","HOME","MENU"},false);
    addRow(page,new String[]{"VOL+","UP","CH+"},false);
    addRow(page,new String[]{"LEFT","OK","RIGHT"},false);
    addRow(page,new String[]{"VOL-","DOWN","CH-"},false);
    addRow(page,new String[]{"BACK","EXIT","INFO"},false);
    for(int x=1;x<=9;x+=3)addRow(page,new String[]{""+x,""+(x+1),""+(x+2)},false);
    addRow(page,new String[]{"-","0","ENTER"},false);

    TextView guide=label("MẸO: nhấn giữ phím để thay mã IR riêng",13,true,Color.WHITE);
    guide.setPadding(0,dp(22),0,dp(8));page.addView(guide);
    TextView note=label("Bộ mã NEC1 lấy từ thư viện IRDB cho một số TV Toshiba, không đảm bảo khớp mọi model. ERF3c98 là phiên bản RC, không phải mã lệnh IR. HOME và các phím điều hướng không có mã mặc định đã xác minh; có thể nhập thủ công.",12,false,0xffaac0dc);
    page.addView(note);
    Button restore=new Button(this);restore.setText("KHÔI PHỤC MÃ MẶC ĐỊNH");restore.setAllCaps(false);
    LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.topMargin=dp(14);page.addView(restore,rp);
    restore.setOnClickListener(v->{prefs.edit().clear().apply();status.setText("Đã khôi phục bộ mã Toshiba NEC1");});
    setContentView(scroll);
  }

  private void loadToshibaNEC1() {
    // IRDB: Toshiba/TV/64,-1.csv : NEC1 device 64, subdevice -1.
    // 40 BF = device 0x40 followed by its complement; command and inverse follow.
    String[] entries={
      "POWER:18","MUTE:16","SOURCE:15","MENU:128","VOL+:26","VOL-:30",
      "CH+:27","CH-:31","EXIT:88","INFO:28","ENTER:23",
      "0:0","1:1","2:2","3:3","4:4","5:5","6:6","7:7","8:8","9:9"
    };
    for(String entry:entries){
      String[] parts=entry.split(":");
      int cmd=Integer.parseInt(parts[1]);
      builtins.put(parts[0],String.format(Locale.US,"40BF%02X%02X",cmd,(cmd^255)));
    }
  }

  private TextView label(String value,int sp,boolean bold,int color){
    TextView text=new TextView(this);text.setText(value);text.setTextSize(sp);text.setTextColor(color);
    if(bold)text.setTypeface(null,Typeface.BOLD);return text;
  }
  private void addRow(LinearLayout page,String[] keys,boolean emphasize){
    LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
    page.addView(row,new LinearLayout.LayoutParams(-1,dp(emphasize?70:59)));
    for(String key:keys){
      Button button=new Button(this);
      button.setAllCaps(false);button.setText(key);button.setTextSize(emphasize?17:13);button.setTextColor(Color.WHITE);
      GradientDrawable background=new GradientDrawable();
      background.setColor(key.equals("POWER")?0xffc34651:emphasize?0xff28496d:surface);
      background.setCornerRadius(dp(15));button.setBackground(background);
      LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(0,-1,1);
      layout.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(button,layout);
      button.setOnClickListener(v->send(key));
      button.setOnLongClickListener(v->{editCode(key);return true;});
    }
  }
  private int dp(int px){return (int)(px*getResources().getDisplayMetrics().density+0.5f);}
  private void editCode(String key){
    EditText input=new EditText(this);
    input.setSingleLine(true);input.setHint("40BF12ED");input.setSelectAllOnFocus(true);
    input.setText(prefs.getString(key,builtins.getOrDefault(key,"")));
    new AlertDialog.Builder(this).setTitle("Mã IR cho "+key)
      .setMessage("Nhập 8 ký tự hex (NEC 32-bit, ví dụ 40BF12ED). Đây không phải mã ERF3c98.")
      .setView(input).setNegativeButton("Hủy",null)
      .setNeutralButton("Dùng mặc định",(d,w)->{
          prefs.edit().remove(key).apply();status.setText("Đã đặt lại phím "+key);
      }).setPositiveButton("Lưu",(d,w)->{
        String code=input.getText().toString().trim().replaceFirst("(?i)^0x","").toUpperCase(Locale.US);
        if(!code.matches("[0-9A-F]{8}")){Toast.makeText(this,"Mã phải có 8 ký tự hex",Toast.LENGTH_LONG).show();return;}
        prefs.edit().putString(key,code).apply();status.setText("Đã lưu "+key+": "+code);
      }).show();
  }
  private void send(String key){
    if(transmitter==null||!transmitter.hasIrEmitter()){
      status.setText("Không tìm thấy mắt phát IR");return;
    }
    String code=prefs.getString(key,builtins.get(key));
    if(code==null){editCode(key);return;}
    try{
      int[] pulses=nec32(code);
      transmitter.transmit(38000,pulses);
      status.setText("Đã phát: "+key+"  •  "+code);
    }catch(Exception ex){
      status.setText("Không phát được: "+ex.getClass().getSimpleName());
    }
  }
  private int[] nec32(String hex){
    long data=Long.parseLong(hex,16);
    int[] pulses=new int[67];
    pulses[0]=9000;pulses[1]=4500;
    int i=2;
    for(int index=0;index<4;index++){
      int b=(int)((data >>> (24-8*index))&255);
      for(int bit=0;bit<8;bit++){
        pulses[i++]=562;
        pulses[i++]=((b>>bit)&1)!=0?1687:562;
      }
    }
    pulses[i]=562;
    return pulses;
  }
}

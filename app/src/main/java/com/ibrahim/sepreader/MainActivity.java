package com.ibrahim.sepreader;

import android.app.*;import android.content.*;import android.content.res.ColorStateList;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.*;import android.net.Uri;import android.os.*;import android.view.*;import android.view.inputmethod.EditorInfo;import android.webkit.*;import android.widget.*;import java.io.*;import java.util.*;import org.json.JSONArray;

public class MainActivity extends Activity {
 private LinearLayout root, content, bar; private ScrollView sc; private WebView web; private EditText search;
 private TextView star, offlineBtn, themeBtn, titleTv; private View dividerView;
 private String theme="paper"; // paper | dim | black | sepia
 private final String HOME="https://plato.stanford.edu/"; private SharedPreferences prefs;
 private static final String SEP="\u0001";

 int bg(){switch(theme){case "dim":return Color.parseColor("#121212");case "black":return Color.BLACK;case "sepia":return Color.parseColor("#F4ECD8");default:return Color.parseColor("#F7F4EE");}}
 int card(){switch(theme){case "dim":return Color.parseColor("#1E1E1E");case "black":return Color.parseColor("#141414");case "sepia":return Color.parseColor("#FBF4E4");default:return Color.WHITE;}}
 int ink(){switch(theme){case "dim":return Color.parseColor("#ECE8E1");case "black":return Color.parseColor("#E5E1D8");case "sepia":return Color.parseColor("#3B2F22");default:return Color.parseColor("#1C1B18");}}
 int muted(){switch(theme){case "dim":return Color.parseColor("#A39C8E");case "black":return Color.parseColor("#8F897C");case "sepia":return Color.parseColor("#7A6A54");default:return Color.parseColor("#6F6A61");}}
 int accent(){switch(theme){case "dim":case "black":return Color.parseColor("#D8A25E");case "sepia":return Color.parseColor("#7A4B21");default:return Color.parseColor("#8A5A2B");}}
 int ripple(){return isDark()?Color.parseColor("#33FFFFFF"):Color.parseColor("#1A000000");}
 boolean isDark(){return theme.equals("dim")||theme.equals("black");}
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(ink());t.setPadding(dp(18),dp(10),dp(18),dp(10));return t;}
 Drawable cardBg(){GradientDrawable g=new GradientDrawable();g.setColor(card());g.setCornerRadius(dp(14));return new RippleDrawable(ColorStateList.valueOf(ripple()),g,null);}

 @Override public void onCreate(Bundle b){super.onCreate(b); prefs=getSharedPreferences("sep",0); theme=prefs.getString("theme","paper"); buildHome();}

 void buildHome(){
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bg());

  bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
  titleTv=tv("SEP Reader",21);titleTv.setTypeface(Typeface.DEFAULT,Typeface.BOLD);titleTv.setOnClickListener(v->goHome());bar.addView(titleTv,new LinearLayout.LayoutParams(0,dp(64),1));
  TextView aa=tv("Aa",18);aa.setGravity(Gravity.CENTER);aa.setTextColor(accent());aa.setOnClickListener(v->showReadingSettings());bar.addView(aa,new LinearLayout.LayoutParams(dp(46),dp(64)));
  offlineBtn=tv("⬇",19);offlineBtn.setGravity(Gravity.CENTER);offlineBtn.setTextColor(accent());offlineBtn.setOnClickListener(v->toggleOffline());bar.addView(offlineBtn,new LinearLayout.LayoutParams(dp(46),dp(64)));
  star=tv("☆",21);star.setGravity(Gravity.CENTER);star.setTextColor(accent());star.setOnClickListener(v->toggleBookmark());bar.addView(star,new LinearLayout.LayoutParams(dp(46),dp(64)));
  themeBtn=tv(isDark()?"☀":"☾",21);themeBtn.setGravity(Gravity.CENTER);themeBtn.setTextColor(accent());themeBtn.setOnClickListener(v->quickToggleTheme());bar.addView(themeBtn,new LinearLayout.LayoutParams(dp(52),dp(64)));
  root.addView(bar);

  dividerView=new View(this);dividerView.setBackgroundColor(isDark()?Color.parseColor("#262626"):Color.parseColor("#E7E1D6"));root.addView(dividerView,new LinearLayout.LayoutParams(-1,dp(1)));

  search=new EditText(this);search.setHint("Search philosophy…");search.setHintTextColor(muted());search.setTextColor(ink());search.setSingleLine();search.setBackground(null);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);search.setPadding(dp(18),0,dp(18),0);root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));search.setOnEditorActionListener((v,a,e)->{openSearch(search.getText().toString());return true;});

  content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(10),dp(6),dp(10),dp(10));sc=new ScrollView(this);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));

  TextView intro=tv("Stanford Encyclopedia of Philosophy",26);intro.setTypeface(Typeface.DEFAULT,Typeface.BOLD);intro.setPadding(dp(8),dp(18),dp(8),dp(2));content.addView(intro);
  TextView sub=tv("A quiet, focused reader for one of the world's major philosophical reference works.",15);sub.setTextColor(muted());sub.setPadding(dp(8),0,dp(8),dp(16));content.addView(sub);

  addSection("Explore", new String[]{"Featured entries","Table of contents","Recently read","Bookmarks","Saved offline"},
   new Runnable[]{()->open(HOME),()->open(HOME+"contents.html"),()->showEntries("Recently read","recent"),()->showEntries("Bookmarks","bookmarks"),()->showOffline()});

  TextView note=tv("Tip\nTap ☆ to bookmark, ⬇ to save an article for offline reading, and Aa to change the reading theme or text size.",14);note.setTextColor(muted());note.setBackground(cardBg());note.setPadding(dp(16),dp(14),dp(16),dp(14));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(dp(8),dp(20),dp(8),dp(8));content.addView(note,np);

  setContentView(root); updateStar();
 }

 void addSection(String head,String[] labels,Runnable[] actions){
  TextView h=tv(head.toUpperCase(Locale.ROOT),13);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setTextColor(muted());if(Build.VERSION.SDK_INT>=21)h.setLetterSpacing(0.08f);h.setPadding(dp(8),dp(6),dp(8),dp(8));content.addView(h);
  for(int i=0;i<labels.length;i++){
   TextView x=tv(labels[i]+"   ›",16);x.setTextColor(ink());x.setBackground(cardBg());
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(8),dp(4),dp(8),dp(4));
   final Runnable act=actions[i];x.setOnClickListener(v->act.run());
   content.addView(x,lp);
  }
 }

 void openSearch(String q){if(q.trim().isEmpty())return;String u="https://plato.stanford.edu/search/searcher.py?query="+Uri.encode(q);open(u);}

 void open(String url){
  if(web!=null) root.removeView(web);
  web=new WebView(this); web.setBackgroundColor(bg());
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setBuiltInZoomControls(false);s.setTextZoom(prefs.getInt("zoom",100));
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,String u){return false;}
   @Override public void onPageFinished(WebView v,String u){inject();String t=v.getTitle();saveRecent((t==null||t.isEmpty())?u:t,u);updateStar();}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onProgressChanged(WebView v,int p){if(p>40)inject();}
  });
  web.loadUrl(url); sc.setVisibility(View.GONE); root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); updateStar();
 }

 void openOffline(String filename,String url){
  if(web!=null) root.removeView(web);
  web=new WebView(this); web.setBackgroundColor(bg());
  web.getSettings().setJavaScriptEnabled(true);web.getSettings().setTextZoom(prefs.getInt("zoom",100));
  try{ File f=new File(getFilesDir(),"offline/"+filename); String html=readFile(f); web.loadDataWithBaseURL(url,html,"text/html","utf-8",null); }
  catch(IOException e){ Toast.makeText(this,"Couldn't open saved article",Toast.LENGTH_SHORT).show(); }
  sc.setVisibility(View.GONE); root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); updateStar();
 }

 void goHome(){ if(web!=null){root.removeView(web);web=null;} buildHome(); }

 void refreshArticleChrome(){
  root.setBackgroundColor(bg());
  titleTv.setTextColor(ink());
  offlineBtn.setTextColor(accent());
  star.setTextColor(accent());
  themeBtn.setTextColor(accent());themeBtn.setText(isDark()?"☀":"☾");
  dividerView.setBackgroundColor(isDark()?Color.parseColor("#262626"):Color.parseColor("#E7E1D6"));
  if(web!=null) web.setBackgroundColor(bg());
  inject();
 }

 void quickToggleTheme(){ theme = theme.equals("paper") ? "dim" : "paper"; prefs.edit().putString("theme",theme).apply(); onThemeChanged(); }

 void onThemeChanged(){ if(web!=null) refreshArticleChrome(); else buildHome(); }

 void showReadingSettings(){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(24),dp(12),dp(24),dp(4));

  TextView themeLabel=tv("Theme",14);themeLabel.setTextColor(muted());themeLabel.setPadding(0,0,0,dp(6));box.addView(themeLabel);
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
  String[] names={"paper","dim","black","sepia"};String[] labels={"Paper","Dim","Black","Sepia"};
  for(int i=0;i<names.length;i++){
   final String tName=names[i];
   TextView swatch=tv(labels[i],13);swatch.setGravity(Gravity.CENTER);swatch.setPadding(dp(10),dp(10),dp(10),dp(10));
   swatch.setBackground(cardBg());
   if(theme.equals(tName)){ GradientDrawable gd=new GradientDrawable();gd.setColor(card());gd.setCornerRadius(dp(10));gd.setStroke(dp(2),accent());swatch.setBackground(gd); }
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(4),0,dp(4),0);
   swatch.setOnClickListener(v->{ theme=tName; prefs.edit().putString("theme",theme).apply(); onThemeChanged(); });
   row.addView(swatch,lp);
  }
  box.addView(row,new LinearLayout.LayoutParams(-1,-2));

  TextView sizeLabel=tv("Text size",14);sizeLabel.setTextColor(muted());sizeLabel.setPadding(0,dp(18),0,dp(6));box.addView(sizeLabel);
  LinearLayout sizeRow=new LinearLayout(this);sizeRow.setOrientation(LinearLayout.HORIZONTAL);sizeRow.setGravity(Gravity.CENTER_VERTICAL);
  TextView minus=tv("A−",18);minus.setGravity(Gravity.CENTER);minus.setBackground(cardBg());minus.setPadding(dp(20),dp(10),dp(20),dp(10));
  TextView pct=tv(prefs.getInt("zoom",100)+"%",15);pct.setGravity(Gravity.CENTER);
  TextView plus=tv("A+",18);plus.setGravity(Gravity.CENTER);plus.setBackground(cardBg());plus.setPadding(dp(20),dp(10),dp(20),dp(10));
  minus.setOnClickListener(v->{int z=Math.max(70,prefs.getInt("zoom",100)-15);prefs.edit().putInt("zoom",z).apply();pct.setText(z+"%");if(web!=null)web.getSettings().setTextZoom(z);});
  plus.setOnClickListener(v->{int z=Math.min(160,prefs.getInt("zoom",100)+15);prefs.edit().putInt("zoom",z).apply();pct.setText(z+"%");if(web!=null)web.getSettings().setTextZoom(z);});
  LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(0,-2,1);mlp.setMargins(0,0,dp(8),0);
  LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(0,-2,1);plp.setMargins(dp(8),0,0,0);
  sizeRow.addView(minus,mlp);sizeRow.addView(pct,new LinearLayout.LayoutParams(dp(60),-2));sizeRow.addView(plus,plp);
  box.addView(sizeRow,new LinearLayout.LayoutParams(-1,-2));

  new AlertDialog.Builder(this).setTitle("Reading settings").setView(box).setPositiveButton("Done",null).show();
 }

 void inject(){
  if(web==null)return;
  String bgc=String.format("#%06X",0xFFFFFF & bg()),cardc=String.format("#%06X",0xFFFFFF & card()),fgc=String.format("#%06X",0xFFFFFF & ink()),linkc=String.format("#%06X",0xFFFFFF & accent());
  String border=isDark()?"rgba(255,255,255,0.10)":"rgba(0,0,0,0.10)";
  String css="html,body{background:"+bgc+" !important;color:"+fgc+" !important;}"
   +"*{background-color:transparent !important;border-color:"+border+" !important;box-shadow:none !important;}"
   +"a,a:visited{color:"+linkc+" !important;}"
   +"table,pre,code,blockquote,.pullquote,.note,#toc{background:"+cardc+" !important;}"
   +"img{opacity:.94;}"
   +"::selection{background:"+linkc+";color:"+bgc+";}";
  String js="javascript:(function(){var s=document.getElementById('sepReaderStyle');if(!s){s=document.createElement('style');s.id='sepReaderStyle';document.head.appendChild(s);}s.innerHTML="+org.json.JSONObject.quote(css)+";})()";
  web.evaluateJavascript(js,null);
 }

 List<String> loadList(String key){List<String> l=new ArrayList<>();for(String line:prefs.getString(key,"").split("\n")) if(!line.trim().isEmpty()) l.add(line); return l;}
 void saveList(String key,List<String> l){prefs.edit().putString(key,String.join("\n",l)).apply();}
 void saveRecent(String title,String url){List<String> l=loadList("recent");l.removeIf(e->e.endsWith(SEP+url));l.add(0,title+SEP+url);while(l.size()>10)l.remove(l.size()-1);saveList("recent",l);}
 boolean isBookmarked(String url){if(url==null)return false;for(String e:loadList("bookmarks")) if(e.endsWith(SEP+url)) return true; return false;}
 void toggleBookmark(){
  if(web==null){Toast.makeText(this,"Open an article first, then tap ☆ to save it.",Toast.LENGTH_SHORT).show();return;}
  String url=web.getUrl(); String t=web.getTitle(); if(t==null||t.isEmpty())t=url;
  List<String> l=loadList("bookmarks");
  if(isBookmarked(url)){l.removeIf(e->e.endsWith(SEP+url));saveList("bookmarks",l);Toast.makeText(this,"Removed bookmark",Toast.LENGTH_SHORT).show();}
  else{l.add(0,t+SEP+url);while(l.size()>50)l.remove(l.size()-1);saveList("bookmarks",l);Toast.makeText(this,"Bookmarked",Toast.LENGTH_SHORT).show();}
  updateStar();
 }
 void updateStar(){if(star==null)return; boolean on=web!=null && isBookmarked(web.getUrl()); star.setText(on?"★":"☆"); updateOfflineIcon();}

 List<String> loadOffline(){List<String> l=new ArrayList<>();for(String line:prefs.getString("offline","").split("\n")) if(!line.trim().isEmpty()) l.add(line); return l;}
 void saveOfflineList(List<String> l){prefs.edit().putString("offline",String.join("\n",l)).apply();}
 String offlineFilenameFor(String url){for(String e:loadOffline()){String[] p=e.split(SEP);if(p.length==3 && p[1].equals(url)) return p[2];} return null;}
 boolean isOffline(String url){return offlineFilenameFor(url)!=null;}
 void updateOfflineIcon(){if(offlineBtn==null)return; boolean on=web!=null && isOffline(web.getUrl()); offlineBtn.setText(on?"✔":"⬇");}
 void toggleOffline(){
  if(web==null){Toast.makeText(this,"Open an article first, then tap ⬇ to save it offline.",Toast.LENGTH_SHORT).show();return;}
  final String url=web.getUrl(); final String t0=web.getTitle(); final String t=(t0==null||t0.isEmpty())?url:t0;
  String existing=offlineFilenameFor(url);
  if(existing!=null){
   new File(getFilesDir(),"offline/"+existing).delete();
   List<String> l=loadOffline(); l.removeIf(e->e.endsWith(SEP+existing)); saveOfflineList(l);
   Toast.makeText(this,"Removed offline copy",Toast.LENGTH_SHORT).show(); updateOfflineIcon(); return;
  }
  web.evaluateJavascript("document.documentElement.outerHTML",value->{
   try{
    String html=new JSONArray("["+value+"]").getString(0);
    String filename=Integer.toHexString(url.hashCode())+".html";
    File dir=new File(getFilesDir(),"offline"); if(!dir.exists()) dir.mkdirs();
    writeFile(new File(dir,filename),html);
    List<String> l=loadOffline(); l.removeIf(e->{String[] p=e.split(SEP);return p.length==3 && p[1].equals(url);});
    l.add(0,t+SEP+url+SEP+filename); saveOfflineList(l);
    Toast.makeText(this,"Saved for offline reading",Toast.LENGTH_SHORT).show(); updateOfflineIcon();
   }catch(Exception ex){ Toast.makeText(this,"Couldn't save this article",Toast.LENGTH_SHORT).show(); }
  });
 }
 void writeFile(File f,String content) throws IOException{ FileOutputStream fos=new FileOutputStream(f); try{ fos.write(content.getBytes("UTF-8")); } finally { fos.close(); } }
 String readFile(File f) throws IOException{ StringBuilder sb=new StringBuilder(); BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8")); try{ String line; while((line=r.readLine())!=null){ sb.append(line).append('\n'); } } finally { r.close(); } return sb.toString(); }

 void showEntries(String title,String key){
  List<String> l=loadList(key);
  if(l.isEmpty()){new AlertDialog.Builder(this).setTitle(title).setMessage(key.equals("bookmarks")?"No bookmarks yet. Open an article and tap ☆ to save it here.":"No recent entries yet. Articles you open will appear here.").setPositiveButton("OK",null).show();return;}
  String[] labels=new String[l.size()];String[] urls=new String[l.size()];
  for(int i=0;i<l.size();i++){int p=l.get(i).indexOf(SEP);labels[i]=p>=0?l.get(i).substring(0,p):l.get(i);urls[i]=p>=0?l.get(i).substring(p+1):l.get(i);}
  new AlertDialog.Builder(this).setTitle(title).setItems(labels,(d,which)->open(urls[which]))
   .setNegativeButton(key.equals("bookmarks")?"Clear all":"Close",key.equals("bookmarks")?(d,which)->{saveList("bookmarks",new ArrayList<>());updateStar();}:null).show();
 }
 void showOffline(){
  List<String> l=loadOffline();
  if(l.isEmpty()){new AlertDialog.Builder(this).setTitle("Saved offline").setMessage("No articles saved yet. Open one and tap ⬇ to save it for offline reading.").setPositiveButton("OK",null).show();return;}
  String[] labels=new String[l.size()];String[] urls=new String[l.size()];String[] files=new String[l.size()];
  for(int i=0;i<l.size();i++){String[] p=l.get(i).split(SEP);labels[i]=p.length>0?p[0]:l.get(i);urls[i]=p.length>1?p[1]:"";files[i]=p.length>2?p[2]:"";}
  new AlertDialog.Builder(this).setTitle("Saved offline").setItems(labels,(d,which)->openOffline(files[which],urls[which]))
   .setNegativeButton("Clear all",(d,which)->{
     for(String e:l){String[] p=e.split(SEP);if(p.length==3) new File(getFilesDir(),"offline/"+p[2]).delete();}
     saveOfflineList(new ArrayList<>()); updateOfflineIcon();
   }).show();
 }

 @Override public void onBackPressed(){if(web!=null && web.canGoBack())web.goBack();else if(web!=null)goHome();else super.onBackPressed();}
}

package com.ibrahim.sepreader;

import android.app.*;import android.content.*;import android.content.res.ColorStateList;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.*;import android.net.Uri;import android.os.*;import android.view.*;import android.view.inputmethod.EditorInfo;import android.webkit.*;import android.widget.*;import java.io.*;import java.util.*;import org.json.JSONArray;

public class MainActivity extends Activity {
 private LinearLayout root, content; private ScrollView sc; private WebView web; private EditText search; private TextView star, offlineBtn; private boolean dark=false; private final String HOME="https://plato.stanford.edu/"; private SharedPreferences prefs;
 private static final String SEP="\u0001";
 int bg(){return dark?Color.parseColor("#121212"):Color.parseColor("#F7F4EE");}
 int card(){return dark?Color.parseColor("#1E1E1E"):Color.WHITE;}
 int ink(){return dark?Color.parseColor("#ECE8E1"):Color.parseColor("#1C1B18");}
 int muted(){return dark?Color.parseColor("#A39C8E"):Color.parseColor("#6F6A61");}
 int accent(){return dark?Color.parseColor("#D8A25E"):Color.parseColor("#8A5A2B");}
 int ripple(){return dark?Color.parseColor("#33FFFFFF"):Color.parseColor("#1A000000");}
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(ink());t.setPadding(dp(18),dp(10),dp(18),dp(10));return t;}
 Drawable cardBg(){GradientDrawable g=new GradientDrawable();g.setColor(card());g.setCornerRadius(dp(14));return new RippleDrawable(ColorStateList.valueOf(ripple()),g,null);}

 @Override public void onCreate(Bundle b){super.onCreate(b); prefs=getSharedPreferences("sep",0); dark=prefs.getBoolean("dark",false); buildHome();}

 void buildHome(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bg());

  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
  TextView title=tv("SEP Reader",22);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);if(android.os.Build.VERSION.SDK_INT>=21)title.setLetterSpacing(0.01f);title.setOnClickListener(v->goHome());bar.addView(title,new LinearLayout.LayoutParams(0,dp(64),1));
  offlineBtn=tv("⬇",20);offlineBtn.setGravity(Gravity.CENTER);offlineBtn.setTextColor(accent());offlineBtn.setOnClickListener(v->toggleOffline());bar.addView(offlineBtn,new LinearLayout.LayoutParams(dp(52),dp(64)));
  star=tv("☆",22);star.setGravity(Gravity.CENTER);star.setTextColor(accent());star.setOnClickListener(v->toggleBookmark());bar.addView(star,new LinearLayout.LayoutParams(dp(52),dp(64)));
  TextView theme=tv(dark?"☀":"☾",22);theme.setGravity(Gravity.CENTER);theme.setTextColor(accent());theme.setOnClickListener(v->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();buildHome();});bar.addView(theme,new LinearLayout.LayoutParams(dp(58),dp(64)));
  root.addView(bar);

  View divider=new View(this);divider.setBackgroundColor(dark?Color.parseColor("#262626"):Color.parseColor("#E7E1D6"));root.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));

  search=new EditText(this);search.setHint("Search philosophy…");search.setHintTextColor(muted());search.setTextColor(ink());search.setSingleLine();search.setBackground(null);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);search.setPadding(dp(18),0,dp(18),0);root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));search.setOnEditorActionListener((v,a,e)->{openSearch(search.getText().toString());return true;});

  content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(10),dp(6),dp(10),dp(10));sc=new ScrollView(this);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));

  TextView intro=tv("Stanford Encyclopedia of Philosophy",26);intro.setTypeface(Typeface.DEFAULT,Typeface.BOLD);intro.setPadding(dp(8),dp(18),dp(8),dp(2));content.addView(intro);
  TextView sub=tv("A quiet, focused reader for one of the world's major philosophical reference works.",15);sub.setTextColor(muted());sub.setPadding(dp(8),0,dp(8),dp(16));content.addView(sub);

  addSection("Explore", new String[]{"Featured entries","Table of contents","Recently read","Bookmarks","Saved offline"},
   new Runnable[]{()->open(HOME),()->open(HOME+"contents.html"),()->showEntries("Recently read","recent"),()->showEntries("Bookmarks","bookmarks"),()->showOffline()});

  TextView note=tv("Tip\nSearch for an entry such as Aristotle, consciousness, free will, existentialism, or philosophy of mind. Tap ☆ while reading to bookmark, or ⬇ to save it for offline reading.",14);note.setTextColor(muted());note.setBackground(cardBg());note.setPadding(dp(16),dp(14),dp(16),dp(14));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(dp(8),dp(20),dp(8),dp(8));content.addView(note,np);

  setContentView(root); updateStar();
 }

 void addSection(String head,String[] labels,Runnable[] actions){
  TextView h=tv(head.toUpperCase(Locale.ROOT),13);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setTextColor(muted());if(android.os.Build.VERSION.SDK_INT>=21)h.setLetterSpacing(0.08f);h.setPadding(dp(8),dp(6),dp(8),dp(8));content.addView(h);
  for(int i=0;i<labels.length;i++){
   TextView x=tv(labels[i],16);x.setTextColor(ink());x.setBackground(cardBg());
   x.setText(labels[i]+"   ›");
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(8),dp(4),dp(8),dp(4));
   final Runnable act=actions[i];x.setOnClickListener(v->act.run());
   content.addView(x,lp);
  }
 }

 void openSearch(String q){if(q.trim().isEmpty())return;String u="https://plato.stanford.edu/search/searcher.py?query="+Uri.encode(q);open(u);}

 void open(String url){
  if(web!=null) root.removeView(web);
  web=new WebView(this); web.setBackgroundColor(bg());
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setBuiltInZoomControls(false);
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
  web.getSettings().setJavaScriptEnabled(true);
  try{ File f=new File(getFilesDir(),"offline/"+filename); String html=readFile(f); web.loadDataWithBaseURL(url,html,"text/html","utf-8",null); }
  catch(IOException e){ Toast.makeText(this,"Couldn't open saved article",Toast.LENGTH_SHORT).show(); }
  sc.setVisibility(View.GONE); root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); updateStar();
 }

 void goHome(){if(web!=null){root.removeView(web);web=null;} sc.setVisibility(View.VISIBLE); updateStar();}

 void inject(){
  if(web==null)return;
  String bg=dark?"#121212":"#F7F4EE",card=dark?"#1E1E1E":"#FFFFFF",fg=dark?"#ECE8E1":"#1C1B18",link=dark?"#D8A25E":"#8A5A2B",border=dark?"rgba(255,255,255,0.10)":"rgba(0,0,0,0.10)";
  String css="html,body{background:"+bg+" !important;color:"+fg+" !important;}"
   +"*{background-color:transparent !important;border-color:"+border+" !important;box-shadow:none !important;}"
   +"a,a:visited{color:"+link+" !important;}"
   +"table,pre,code,blockquote,.pullquote,.note,#toc{background:"+card+" !important;}"
   +"img{opacity:.94;}"
   +"::selection{background:"+link+";color:"+bg+";}";
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
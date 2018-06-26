package me.temoa.expandabletextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by lai
 * on 2018/5/7.
 */

@SuppressWarnings("unused") // public api
public class ExpandableTextView extends AppCompatTextView {

  private String mText = "";

  private int mMaxRows = 3;
  private boolean mIsNoticeHasUnderline;
  private int mNoticeTextColor;
  private String mExpandNotice;
  private String mCollapseNotice;

  private boolean mIsExpand;

  private OnTextExpandedListener mListener;

  public interface OnTextExpandedListener {
    void expanded(boolean expandable);

    void collapse();
  }

  public void setText(@NonNull String text, boolean isExpand) {
    mText = text;
    mIsExpand = isExpand;
    setText(text);
  }

  public void setExpanded(boolean isExpand) {
    mIsExpand = isExpand;
    requestLayout();
  }

  public boolean isExpand() {
    return mIsExpand;
  }

  public void setListener(@NonNull OnTextExpandedListener listener) {
    mListener = listener;
  }

  public ExpandableTextView(Context context) {
    super(context);
    init(null);
  }

  public ExpandableTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void init(AttributeSet attrs) {
    this.setOnTouchListener(new MyLinkMovementMethod());
    mNoticeTextColor = getContext().getResources().getColor(android.R.color.primary_text_light);
    if (attrs != null) {
      TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
      mNoticeTextColor = ta.getColor(R.styleable.ExpandableTextView_etv_notice_text_Color,
              getContext().getResources().getColor(android.R.color.primary_text_light));
      mIsNoticeHasUnderline = ta.getBoolean(R.styleable.ExpandableTextView_etv_notice_underline, true);
      mMaxRows = ta.getInteger(R.styleable.ExpandableTextView_etv_rows, 3);
      mExpandNotice = ta.getString(R.styleable.ExpandableTextView_etv_expand_notice);
      mCollapseNotice = ta.getString(R.styleable.ExpandableTextView_etv_collapse_notice);
      ta.recycle();
    }
    if (TextUtils.isEmpty(mExpandNotice)) {
      mExpandNotice = "收起";
    }
    if (TextUtils.isEmpty(mCollapseNotice)) {
      mCollapseNotice = "全文";
    }
  }

  @SuppressLint("DrawAllocation")
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int totalPadding;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      totalPadding = getPaddingStart() + getPaddingEnd();
    } else {
      totalPadding = getPaddingLeft() + getPaddingRight();
    }
    int totalWidth = getMeasuredWidth() - totalPadding;
    StaticLayout staticLayout = new StaticLayout(
            mText,
            getPaint(),
            totalWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1,
            0,
            true);
    int lineCount = staticLayout.getLineCount();
    if (lineCount > mMaxRows) {
      if (mIsExpand) {
        setText(getClickText(mText + "\n\n" + mExpandNotice));
        if (mListener != null) mListener.expanded(true);
      } else {
        lineCount = mMaxRows;
        float dotWidth = getPaint().measureText("... " + mCollapseNotice);
        int start = staticLayout.getLineStart(lineCount - 1);
        int end = staticLayout.getLineEnd(lineCount - 1);
        String lineText = mText.substring(start, end);

        int endIndex = 0;
        for (int i = lineText.length() - 1; i >= 0; i--) {
          String s = lineText.substring(i, lineText.length());
          if (getPaint().measureText(s) >= dotWidth) {
            endIndex = i;
            break;
          }
        }

        String newEndLineText = lineText.substring(0, endIndex) + "... " + mCollapseNotice;
        String newText = mText.substring(0, start) + newEndLineText;
        setText(getClickText(newText));

        if (mListener != null) mListener.collapse();
      }
    } else {
      setText(mText);
      if (mListener != null) mListener.expanded(false);
    }

    int newHeight = 0;
    int singleLineHeight = 0;
    for (int i = 0; i < lineCount; i++) {
      Rect lineRect = new Rect();
      staticLayout.getLineBounds(i, lineRect);
      singleLineHeight = lineRect.height();
      newHeight += singleLineHeight;
    }
    if (mIsExpand) newHeight += (singleLineHeight * 3);
    newHeight += getPaddingTop() + getPaddingBottom();
    setMeasuredDimension(getMeasuredWidth(), newHeight);
  }

  public SpannableStringBuilder getClickText(String target) {
    SpannableStringBuilder sb = new SpannableStringBuilder(target);
    if (mIsNoticeHasUnderline) {
      sb.setSpan(new ClickableSpan() {
        @Override
        public void onClick(View widget) {
          setExpanded(!mIsExpand);
        }
      }, target.length() - 2, target.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    } else {
      sb.setSpan(new NoUnderlineClickableSpan(),
              target.length() - 2, target.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
    sb.setSpan(new ForegroundColorSpan(mNoticeTextColor),
            target.length() - 2, target.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    return sb;
  }

  class MyLinkMovementMethod implements OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      boolean ret = false;
      TextView widget = (TextView) v;
      CharSequence text = widget.getText();
      Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
      int action = event.getAction();
      if (action == MotionEvent.ACTION_UP ||
              action == MotionEvent.ACTION_DOWN) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();
        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
        if (link.length != 0) {
          if (action == MotionEvent.ACTION_UP) {
            link[0].onClick(widget);
          }
          ret = true;
        }
      }
      return ret;
    }
  }

  class NoUnderlineClickableSpan extends ClickableSpan {

    @Override
    public void updateDrawState(TextPaint ds) {
      ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
      setExpanded(!mIsExpand);
    }
  }
}

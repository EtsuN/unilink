package com.teamxod.unilink;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

class Question{
    // q is the question and choiceOne/Two are choices
    // booleans are indicators for boxes checked or not. Stored to implement check only one functionality
    String q;
    String choiceOne;
    String choiceTwo;
    boolean firstChecked;
    boolean secondChecked;

    public Question(String q, String choiceOne, String choiceTwo) {
        this.q = q;
        this.firstChecked = false;
        this.secondChecked = false;
        this.choiceOne = choiceOne;
        this.choiceTwo = choiceTwo;
    }
    // getter and setter for fields
    public String getQuestion(){
        return this.q;
    }

    public String getChoiceOne() { return this.choiceOne;}

    public String getChoiceTwo() { return this.choiceTwo;}

    public boolean getfirstChecked(){
        return this.firstChecked;
    }

    public boolean getSecondChecked() {return this.secondChecked;}

    public void setQuestion(String q){
        this.q = q;
    }

    public void setChoiceOne(String s) { this.choiceOne = s;}

    public void setChoiceTwo(String s) { this.choiceTwo = s;}

    public void setfirstChecked(boolean checked){
        this.firstChecked = checked;
    }

    public void setSecondChecked(boolean checked) {this.secondChecked = checked;}


}
public class preferenceAdaptor extends ArrayAdapter<Question>{
    // viewHolder to potimize the loading speed

    static class ViewHolder {
        private TextView q;
        private CheckedTextView checkOne;
        private CheckedTextView checkTwo;
    }


        private List<Question> questionList;
        private Context context;

        public preferenceAdaptor(Context context, List<Question> questionList) {
            super(context, 0, questionList);
        }

    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // inflate the view if null
        final ViewHolder holder = (view == null? new ViewHolder() : (ViewHolder)view.getTag());
        if(view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.preference_list_item_check_two, parent, false);
            holder.q = (TextView)view.findViewById(R.id.question);
            holder.checkOne = (CheckedTextView)view.findViewById(R.id.checked1);
            holder.checkTwo = (CheckedTextView)view.findViewById(R.id.checked2);
            view.setTag(holder);
        }
        // set Question's text fields
        final Question preference = (Question)getItem(position);

      //  TextView q = (TextView)view.findViewById((R.id.question));
        holder.q.setText(preference.getQuestion());

       // final CheckedTextView checkOne = (CheckedTextView)view.findViewById(R.id.checked1);
        holder.checkOne.setText(preference.getChoiceOne());

       // final CheckedTextView checkTwo = (CheckedTextView)view.findViewById((R.id.checked2));
        holder.checkTwo.setText(preference.getChoiceTwo());

        holder.checkOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!preference.getfirstChecked()){
                    preference.setfirstChecked(true);
                    holder.checkOne.setChecked(true);
                    holder.checkOne.setCheckMarkDrawable(R.drawable.check);
                    preference.setSecondChecked(false);
                    holder.checkTwo.setChecked(false);
                    holder.checkTwo.setCheckMarkDrawable(null);
                }else{
                    preference.setfirstChecked(false);
                    holder.checkOne.setChecked(false);
                    holder.checkOne.setCheckMarkDrawable(null);
                }
            }
        });

        holder.checkTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!preference.getSecondChecked()) {
                    preference.setfirstChecked(false);
                    holder.checkOne.setChecked(false);
                    holder.checkOne.setCheckMarkDrawable(null);
                    preference.setSecondChecked(true);
                    holder.checkTwo.setChecked(true);
                    holder.checkTwo.setCheckMarkDrawable(R.drawable.check);
                }else{
                    preference.setSecondChecked(false);
                    holder.checkTwo.setChecked(false);
                    holder.checkTwo.setCheckMarkDrawable(null);
                }
            }
        });

        return view;
    }
}

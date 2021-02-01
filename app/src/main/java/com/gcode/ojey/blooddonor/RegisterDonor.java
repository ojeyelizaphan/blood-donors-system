package com.gcode.dennis.blooddonor;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterDonor extends Fragment implements AdapterView.OnItemSelectedListener {

    EditText firstName,lastName,email,phoneNumber,dateOfBirth;
    Button btnRegisterDonor;
    DatePickerDialog datePickerDialog;
    DatabaseReference rootRef;
    ProgressDialog dialog;

    String mFirstName;
    String mLastName;
    String mEmail;
    String mPhoneNumber;
    String mDateOfBirth;
    String bloodGroup = "";
    String gender = "";

    private Spinner spinner;
    private static final String[] spinnerItems = {"A+", "B+", "AB+", "O+", "A-","B-","AB-","O-"};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return layout
        return inflater.inflate(R.layout.fragment_register_donor, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set title of current activity
        getActivity().setTitle("Register As Donor");

        //Firebase database reference
         rootRef = FirebaseDatabase.getInstance().getReference();


        //Initialization of views
        firstName = getActivity().findViewById(R.id.firstName);
        lastName = getActivity().findViewById(R.id.lastName);
        email = getActivity().findViewById(R.id.email);
        phoneNumber = getActivity().findViewById(R.id.phoneNumber);
        dateOfBirth = getActivity().findViewById(R.id.DateOfBirth);
        btnRegisterDonor = getActivity().findViewById(R.id.registerDonor);

        dialog = new ProgressDialog(getActivity());

        //Spinner contains all blood types
        spinner = getActivity().findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item, spinnerItems);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Date picker
        dateOfBirth.setInputType(InputType.TYPE_NULL);

        //Onclick listener to display date picker when edittext is clicked
        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date picker dialog
                datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        //set date selected
                        dateOfBirth.setText(dayOfMonth + "/" + month + "/" + year);

                    }
                }, year,month,day);
                datePickerDialog.show();
            }
        });

        //Onclick listener for register button
        btnRegisterDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get values from edittext
                mFirstName = firstName.getText().toString().trim();
                mLastName = lastName.getText().toString().trim();
                mEmail = email.getText().toString().trim();
                mPhoneNumber = phoneNumber.getText().toString().trim();
                mDateOfBirth = dateOfBirth.getText().toString().trim();

                if (validate()) {
                    registerDonor();

                    //Pop alert dialog box
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Thank You!");
                    alert.setMessage("Thank you for registering as a donor." + "\n" + " You will receive a notification when a receiver requests for blood");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);

                        }
                    });
                    alert.show();
                }
            }
        });

    }



    private void registerDonor() {

        //Show progress dialog
        dialog.setMessage("Registering...");
        dialog.show();

        //Save values to map
        final Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put("First Name", mFirstName);
        keyMap.put("Last Name", mLastName);
        keyMap.put("Email", mEmail);
        keyMap.put("Phone Number", mPhoneNumber);
        keyMap.put("Date of Birth", mDateOfBirth);
        keyMap.put("Blood Group", bloodGroup);
        keyMap.put("Gender", gender);

        //Save registration details to database under Donors table
        rootRef.child("Donors").setValue(keyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //Hide progress dialog
                dialog.dismiss();

                //display toast if task is not successful
                if (!task.isSuccessful() && task.getException() != null) {

                    Toast.makeText(getActivity(), "Database error! Please register again", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Check if every field has been filled
    private boolean validate() {

        if (TextUtils.isEmpty(mFirstName))
        {
            Toast.makeText(getActivity(), "Please enter your First Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(mLastName))
        {
            Toast.makeText(getActivity(), "Please enter your last name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(mEmail))
        {
            Toast.makeText(getActivity(), "Please enter your email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(mPhoneNumber))
        {
            Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (TextUtils.isEmpty(mDateOfBirth))
        {
            Toast.makeText(getActivity(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //Spinner items selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        //switch statement
        switch (position) {
            case 0:
                bloodGroup = "A+";
                break;

            case 1:
                bloodGroup = "B+";
                break;

            case 2:
                bloodGroup = "AB+";
                break;

            case 3:
                bloodGroup = "O+";
                break;

            case 4:
                bloodGroup = "A-";
                break;

            case 5:
                bloodGroup = "B-";
                break;

            case 6:
                bloodGroup = "AB-";
                break;

            case 7:
                bloodGroup = "O-";
                break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void gender(View view) {

        //Check which radio button is checked
        boolean isChecked = ((RadioButton) view ).isChecked();

      switch (view.getId()){

          case R.id.male:
              if (isChecked) {
                  gender = "Male";
              }
              break;

          case R.id.female:
              if (isChecked) {
                  gender = "Female";
              }
              break;
      }

    }
}

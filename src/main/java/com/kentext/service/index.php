<?php
    global $user;

    if($user->uid)
    {
        $current_user = user_load($user->uid); // user_load(uid) returns the complete array
        $current_sms_credits = (($current_user->field_sms_credits)['und'][0]['value']);
        $new_credits = $current_sms_credits - 1;
        $current_user->field_sms_credits['und'][0]['value'] = ($new_credits);
        echo ($new_credits);
    }
    else
    {
        print "0";
    }
?>
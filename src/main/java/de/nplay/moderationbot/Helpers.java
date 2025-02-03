package de.nplay.moderationbot;

import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class Helpers {

    public static final ErrorHandler UNKNOWN_USER_HANDLER = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_USER);

}

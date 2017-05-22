package communication;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public enum MsgType {
    announce, // announce a solution
    request, // request to perform this solution instead
    agree, // agree to an announced plan
    cfp,
    propose,
    acceptProposal,
    rejectProposal;
}
package communication;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
// TODO abstract class + 3 implementations (announcement, request, proposal)?
public enum MsgType {
    queryif, inform,
    request, agree, refuse, failure,
    cfp, propose, acceptproposal, rejectProposal;
}
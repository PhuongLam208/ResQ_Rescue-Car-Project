const FeedbackDetail = ({ feedback }) => {
    return (
        <div className="max-h-[600px] overflow-y-auto border border-[#D8D8D8] rounded-2xl">
            <table className="w-full border border-spacing-0 border-[#D8D8D8] rounded-2xl">
                <tr>
                    <td className="font-raleway feedbackHead">Request Rescue No.</td>
                    <td className="font-lexend feedbackContent">
                        {feedback.rrId}
                    </td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Rescue's Rate</td>
                    <td className="font-lexend feedbackContent">
                        <div className="flex flex-row items-center gap-1">
                            {feedback.rateRequest > 0 ? (
                                [...Array(feedback.rateRequest)].map((_, i) => (
                                    <svg
                                        key={i}
                                        className="w-5 h-5 text-yellow-500"
                                        fill="currentColor"
                                        viewBox="0 0 20 20"
                                    >
                                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.97a1 1 0 00.95.69h4.18c.969 0 1.371 1.24.588 1.81l-3.39 2.46a1 1 0 00-.364 1.118l1.287 3.97c.3.921-.755 1.688-1.54 1.118l-3.39-2.46a1 1 0 00-1.175 0l-3.39 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.97a1 1 0 00-.364-1.118l-3.39-2.46c-.783-.57-.38-1.81.588-1.81h4.18a1 1 0 00.95-.69l1.286-3.97z" />
                                    </svg>
                                ))
                            ) : (
                                <span className="text-gray-400 italic">---</span>
                            )}
                        </div>
                    </td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Feedback For Rescue</td>
                    <td className="font-lexend feedbackContent">
                        {feedback.feedbackRequest ? (feedback.feedbackRequest) :
                            (<span className="text-gray-400 italic">---</span>)}
                    </td>
                </tr><tr>
                    <td className="font-raleway feedbackHead">Customer</td>
                    <td className="font-lexend feedbackContent">{feedback.userName}</td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Customer's Rate</td>
                    <td className="font-lexend feedbackContent">
                        <div className="flex flex-row items-center gap-1">
                            {feedback.rateCustomer > 0 ? (
                                [...Array(feedback.rateCustomer)].map((_, i) => (
                                    <svg
                                        key={i}
                                        className="w-5 h-5 text-yellow-500"
                                        fill="currentColor"
                                        viewBox="0 0 20 20"
                                    >
                                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.97a1 1 0 00.95.69h4.18c.969 0 1.371 1.24.588 1.81l-3.39 2.46a1 1 0 00-.364 1.118l1.287 3.97c.3.921-.755 1.688-1.54 1.118l-3.39-2.46a1 1 0 00-1.175 0l-3.39 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.97a1 1 0 00-.364-1.118l-3.39-2.46c-.783-.57-.38-1.81.588-1.81h4.18a1 1 0 00.95-.69l1.286-3.97z" />
                                    </svg>
                                ))
                            ) : (
                                <span className="text-gray-400 italic">---</span>
                            )}
                        </div>
                    </td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Feedback For Customer</td>
                    <td className="font-lexend feedbackContent">
                        {feedback.feedbackCustomer ? feedback.feedbackCustomer :
                            (<span className="text-gray-400 italic">---</span>)}
                    </td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Parnter</td>
                    <td className="font-lexend feedbackContent">{feedback.partnerName}</td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Parnter's Rate</td>
                    <td className="font-lexend feedbackContent">
                        <div className="flex flex-row items-center gap-1">
                            {feedback.ratePartner > 0 ? (
                                [...Array(feedback.ratePartner)].map((_, i) => (
                                    <svg
                                        key={i}
                                        className="w-5 h-5 text-yellow-500"
                                        fill="currentColor"
                                        viewBox="0 0 20 20"
                                    >
                                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.97a1 1 0 00.95.69h4.18c.969 0 1.371 1.24.588 1.81l-3.39 2.46a1 1 0 00-.364 1.118l1.287 3.97c.3.921-.755 1.688-1.54 1.118l-3.39-2.46a1 1 0 00-1.175 0l-3.39 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.97a1 1 0 00-.364-1.118l-3.39-2.46c-.783-.57-.38-1.81.588-1.81h4.18a1 1 0 00.95-.69l1.286-3.97z" />
                                    </svg>
                                ))
                            ) : (
                                <span className="text-gray-400 italic">---</span>
                            )}
                        </div>
                    </td>
                </tr>
                <tr>
                    <td className="font-raleway feedbackHead">Feedback For Partner</td>
                    <td className="font-lexend feedbackContent">
                        {feedback.feedbackPartner ? feedback.feedbackPartner :
                            (<span className="text-gray-400 italic">---</span>)}
                    </td>
                </tr>
            </table>
        </div>
    )
}

export default FeedbackDetail;
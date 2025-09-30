import FaqItem from "./FaqItem";

const faqs = [
  {
    question: "What services does vehicle rescue include?",
    answer:
      "Our rescue services include towing, on-site repairs, jump-starting dead batteries, tire replacement, and minor troubleshooting to get you back on the road as quickly as possible.",
  },
  {
    question: "Where does the service operate?",
    answer:
      "ResQ operates in major cities and surrounding areas across the country. We’re constantly expanding our service coverage to ensure more drivers are supported anywhere, anytime.",
  },
  {
    question: "How are rescue fees calculated?",
    answer:
      "All pricing is transparent and shown clearly within the app before you confirm your booking. Costs may vary depending on distance, service type, and time of request.",
  },
  {
    question: "Do I need to book in advance?",
    answer:
      "You can request immediate rescue for emergencies or choose to schedule a service in advance at a time that’s convenient for you.",
  },
  {
    question: "How long does it take for a rescue team to arrive?",
    answer:
      "Typically, our rescue team will arrive within 15 to 30 minutes depending on your location and traffic conditions. We always strive to be as fast as possible.",
  },
  {
    question: "Is the service available at night or on holidays?",
    answer:
      "Yes. ResQ operates 24/7, including nighttime, weekends, and public holidays. You can rely on us whenever you need support.",
  },
  {
    question: "How do I pay for the service?",
    answer:
      "We support multiple payment methods including cash, e-wallets (e.g., Momo, ZaloPay), and credit/debit cards. You can choose the method that works best for you right in the app.",
  },
];

const FaqSection = () => {
  return (
    <section className="max-w-[1072px] mx-auto mt-12 px-4">
      <div className="text-center mb-8">
        <p className="text-sm font-manrope uppercase tracking-wide text-gray-600">FAQ</p>
        <h2 className="text-3xl font-bold font-manrope text-[#E2311D]">Frequently Asked Questions</h2>
      </div>

      <div className="grid md:grid-cols-2 gap-10 items-start">
        {/* FAQ list */}
        <div>
          {faqs.map((item, index) => (
            <FaqItem key={index} question={item.question} answer={item.answer} />
          ))}
        </div>

        {/* Illustration */}
        <div className="flex justify-center">
          <img
            src="/images/2348982.jpg"
            alt="FAQ Illustration"
            className="w-full max-w-sm rounded-md"
          />
        </div>
      </div>
    </section>
  );
};

export default FaqSection;

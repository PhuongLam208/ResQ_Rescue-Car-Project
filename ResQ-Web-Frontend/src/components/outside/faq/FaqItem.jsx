import { useState } from "react";

const FaqItem = ({ question, answer }) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div
      className="border-b py-4 cursor-pointer"
      onClick={() => setIsOpen(!isOpen)}
    >
      <div className="flex justify-between font-lexend items-center text-base font-semibold text-gray-800">
        <span>{question}</span>
        {/* Icon bo tròn */}
        <span className="w-6 h-6 flex items-center justify-center rounded-full bg-gray-200 text-gray-700 text-lg">
          {isOpen ? "−" : "+"}
        </span>
      </div>
      {isOpen && (
        <div className="mt-3 font-manrope text-gray-600 text-[16px] leading-relaxed">
          {answer}
        </div>
      )}
    </div>
  );
};

export default FaqItem;

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { appliance, problem } = req.body;

  if (!appliance || !problem) {
    return res.status(400).json({ message: 'Missing appliance or problem description' });
  }

  const text = (appliance + ' ' + problem).toLowerCase();

  // Smart keyword-based diagnostic rule mapping
  let cause = 'General wear-and-tear of internal electronic relays or physical connectors.';
  let urgency = 'Medium';
  let recommendedType = 'Electrician';
  let tips = [
    'Clean dust from vent filters and check external power outlet stability.',
    'Do not force operate the unit; keep unplugged if overheating occurs.',
    'Schedule routine maintenance checks twice a year to prolong life.'
  ];

  if (text.includes('spin') || text.includes('drum') || text.includes('cycle') || text.includes('wash')) {
    cause = 'Damaged drive belt, worn-out motor carbon brushes, or a malfunctioning lid switch sensor.';
    urgency = 'Medium';
    recommendedType = 'Electrician';
    tips = [
      'Avoid overloading the washing drum with heavy laundry piles.',
      'Check if the inlet hoses are completely straight and free of kinks.',
      'Unplug power cord immediately if you hear metal-scraping sounds.'
    ];
  } else if (text.includes('leak') || text.includes('water') || text.includes('drain') || text.includes('flood')) {
    cause = 'Clogged drain pipe, compromised inlet valve gaskets, or split pump housing sealant.';
    urgency = 'High';
    recommendedType = 'Electrician';
    tips = [
      'Turn off the main supply faucet connected to the appliance immediately.',
      'Place absorption towels around the base to prevent electrical shorting.',
      'Ensure the appliance resides on completely flat level flooring.'
    ];
  } else if (text.includes('screen') || text.includes('display') || text.includes('software') || text.includes('ram') || text.includes('boot') || text.includes('virus') || text.includes('os') || text.includes('computer') || text.includes('laptop')) {
    cause = 'Failed graphics chip connector, corrupt storage sector, or power supply unit capacitor breakdown.';
    urgency = 'Low';
    recommendedType = 'Computer';
    tips = [
      'Back up critical files to external cloud storage before servicing.',
      'Disconnect all accessory USB cables and perform a hard electrical reset.',
      'Operate on non-static surfaces and prevent static electricity shocks.'
    ];
  } else if (text.includes('power') || text.includes('dead') || text.includes('fuse') || text.includes('spark') || text.includes('turn on') || text.includes('tripping')) {
    cause = 'Short circuit in internal transformer wiring coils or cracked power terminal soldering points.';
    urgency = 'Critical';
    recommendedType = 'Electrician';
    tips = [
      'DO NOT touch the plug if you smell melted plastics or wire smoke.',
      'Shut off the corresponding circuit breaker in your residential fuse box.',
      'Keep dry; do not apply liquid cleaning agents directly on the unit.'
    ];
  } else if (text.includes('heat') || text.includes('cold') || text.includes('freeze') || text.includes('ice') || text.includes('temp') || text.includes('cooling')) {
    cause = 'Faulty bimetal thermostat sensor, defrost heater breakdown, or depleted refrigerant lines.';
    urgency = 'High';
    recommendedType = 'Electrician';
    tips = [
      'Verify if condenser coils are clear of heavy dust build-up.',
      'Ensure doors seal tightly and gaskets are clean of sticky spills.',
      'Keep the appliance at least 15cm away from walls to sustain airflow.'
    ];
  }

  // Indian Market Pricing & Details
  const resData = {
    appliance,
    problem,
    likelyCategory: recommendedType,
    cause,
    urgency,
    recommendedWorkerType: recommendedType,
    confidence: 92,
    estRepairTime: '1 - 2 Hours',
    estCostRange: '₹800 – ₹2,500',
    tips
  };

  if (text.includes('ac') || text.includes('conditioner')) {
    resData.estCostRange = '₹1,200 – ₹6,000';
  } else if (text.includes('fridge') || text.includes('refrigerator')) {
    resData.estCostRange = '₹1,000 – ₹4,000';
  } else if (text.includes('microwave')) {
    resData.estCostRange = '₹500 – ₹2,500';
  } else if (text.includes('heater') || text.includes('geyser')) {
    resData.estCostRange = '₹800 – ₹3,500';
  }

  // Structured response format matching prompt description
  return res.status(200).json(resData);
}

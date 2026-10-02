package com.example.clinico.Helper;

import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Utils.Constants;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleDataHelper {

    public static void seedMedicines() {
        List<ProductModel> products = new ArrayList<>();

        // Product 1
        ProductModel p1 = new ProductModel();
        p1.setName("Paracetamol 500mg");
        p1.setGenericName("Paracetamol");
        p1.setBrand("Napa");
        p1.setCategory("Pain Relief");
        p1.setPrice(25.0);
        p1.setStock(100);
        p1.setAvailable(true);
        p1.setDescription("Fast effective relief of fever and pain such as headache, toothache and backache.");
        p1.setUsage("Take 1-2 tablets every 4-6 hours as needed.");
        p1.setWarnings("Do not exceed 8 tablets in 24 hours.");
        p1.setMainImage("https://res.cloudinary.com/demo/image/upload/v1611000000/sample.jpg");
        products.add(p1);

        // Product 2
        ProductModel p2 = new ProductModel();
        p2.setName("Vitamin C 1000mg");
        p2.setGenericName("Ascorbic Acid");
        p2.setBrand("Ceevit");
        p2.setCategory("Vitamins");
        p2.setPrice(150.0);
        p2.setStock(50);
        p2.setAvailable(true);
        p2.setDescription("Supports immune system health and provides antioxidant support.");
        p2.setUsage("Take 1 tablet daily with food.");
        p2.setWarnings("Consult a doctor if pregnant or nursing.");
        p2.setMainImage("https://res.cloudinary.com/demo/image/upload/v1611000000/sample.jpg");
        products.add(p2);

        // Product 3
        ProductModel p3 = new ProductModel();
        p3.setName("Antacid Suspension");
        p3.setGenericName("Magnesium Hydroxide");
        p3.setBrand("Entacyd");
        p3.setCategory("Digestive Health");
        p3.setPrice(85.0);
        p3.setStock(30);
        p3.setAvailable(true);
        p3.setDescription("Relieves heartburn, acid indigestion and sour stomach.");
        p3.setUsage("2-4 teaspoons between meals and at bedtime.");
        p3.setWarnings("Shake well before use.");
        p3.setMainImage("https://res.cloudinary.com/demo/image/upload/v1611000000/sample.jpg");
        products.add(p3);

        // Product 4
        ProductModel p4 = new ProductModel();
        p4.setName("Cetirizine 10mg");
        p4.setGenericName("Cetirizine Hydrochloride");
        p4.setBrand("Alatrol");
        p4.setCategory("Fever & Cold");
        p4.setPrice(40.0);
        p4.setStock(120);
        p4.setAvailable(true);
        p4.setDescription("Relief of allergy symptoms such as sneezing, runny nose and itchy eyes.");
        p4.setUsage("Take 1 tablet once daily.");
        p4.setWarnings("May cause drowsiness.");
        p4.setMainImage("https://res.cloudinary.com/demo/image/upload/v1611000000/sample.jpg");
        products.add(p4);

        for (ProductModel product : products) {
            String id = FirebaseDatabase.getInstance().getReference(Constants.NODE_MEDICAL_PRODUCTS).push().getKey();
            if (id != null) {
                product.setProductId(id);
                product.setCreatedAt(System.currentTimeMillis());
                FirebaseDatabase.getInstance().getReference(Constants.NODE_MEDICAL_PRODUCTS).child(id).setValue(product);
            }
        }
    }

    public static void seedDoctors() {
        List<DoctorsModel> doctors = new ArrayList<>();

        // Doctor 1
        DoctorsModel doc1 = new DoctorsModel();
        doc1.setName("Dr. Sarah Johnson");
        doc1.setSpecial("Cardiologist");
        doc1.setEducation("MBBS, MD (Cardiology)");
        doc1.setExperience(10);
        doc1.setPatiens(3500);
        doc1.setRating(4.9);
        doc1.setReviewCount(190);
        doc1.setLanguages("English, Bangla");
        doc1.setLocation("MediClinic Downtown");
        doc1.setPrice(1200);
        doc1.setBiography("Dr. Sarah Johnson is a cardiologist specializing in preventive cardiology, cardiovascular health, hypertension, and heart disease management.");
        doc1.setFeatured(true);
        doc1.setAvailability(getAvailabilitySarah());
        doctors.add(doc1);

        // Doctor 2
        DoctorsModel doc2 = new DoctorsModel();
        doc2.setName("Dr. Nusrat Rahman");
        doc2.setSpecial("Gynecologist");
        doc2.setEducation("MBBS, FCPS (Obstetrics & Gynecology)");
        doc2.setExperience(8);
        doc2.setPatiens(2800);
        doc2.setRating(4.8);
        doc2.setReviewCount(165);
        doc2.setLanguages("Bangla, English");
        doc2.setLocation("CarePoint Women's Clinic");
        doc2.setPrice(1000);
        doc2.setBiography("Dr. Nusrat Rahman specializes in women's health, reproductive health, pregnancy care, menstrual disorders, and gynecological conditions.");
        doc2.setFeatured(true);
        doc2.setAvailability(getAvailabilityNusrat());
        doctors.add(doc2);

        // Doctor 3
        DoctorsModel doc3 = new DoctorsModel();
        doc3.setName("Dr. Arif Hasan");
        doc3.setSpecial("Dermatologist");
        doc3.setEducation("MBBS, DDV");
        doc3.setExperience(7);
        doc3.setPatiens(2400);
        doc3.setRating(4.7);
        doc3.setReviewCount(142);
        doc3.setLanguages("Bangla, English");
        doc3.setLocation("SkinCare Medical Center");
        doc3.setPrice(900);
        doc3.setBiography("Dr. Arif Hasan specializes in dermatology, acne treatment, skin allergies, hair problems, and general skin health.");
        doc3.setFeatured(false);
        doc3.setAvailability(getAvailabilityArif());
        doctors.add(doc3);

        // Doctor 4
        DoctorsModel doc4 = new DoctorsModel();
        doc4.setName("Dr. Michael Chen");
        doc4.setSpecial("General Physician");
        doc4.setEducation("MBBS, MD (Internal Medicine)");
        doc4.setExperience(12);
        doc4.setPatiens(5000);
        doc4.setRating(4.9);
        doc4.setReviewCount(320);
        doc4.setLanguages("English, Bangla");
        doc4.setLocation("MediClinic Downtown");
        doc4.setPrice(800);
        doc4.setBiography("Dr. Michael Chen provides comprehensive primary care, preventive healthcare, common illness treatment, and general medical consultations.");
        doc4.setFeatured(true);
        doc4.setAvailability(getAvailabilityMichael());
        doctors.add(doc4);

        // Doctor 5
        DoctorsModel doc5 = new DoctorsModel();
        doc5.setName("Dr. Omar Ahmed");
        doc5.setSpecial("Orthopedic Specialist");
        doc5.setEducation("MBBS, MS (Orthopedics)");
        doc5.setExperience(11);
        doc5.setPatiens(4100);
        doc5.setRating(4.8);
        doc5.setReviewCount(230);
        doc5.setLanguages("Bangla, English");
        doc5.setLocation("OrthoCare Hospital");
        doc5.setPrice(1100);
        doc5.setBiography("Dr. Omar Ahmed specializes in bone and joint conditions, sports injuries, arthritis, back pain, and orthopedic rehabilitation.");
        doc5.setFeatured(false);
        doc5.setAvailability(getAvailabilityOmar());
        doctors.add(doc5);

        for (DoctorsModel doc : doctors) {
            FirebaseDatabase.getInstance().getReference("Doctors").push().setValue(doc);
        }
    }

    private static Map<String, List<DoctorsModel.TimeSlot>> getAvailabilitySarah() {
        Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
        List<DoctorsModel.TimeSlot> monSlots = new ArrayList<>();
        monSlots.add(new DoctorsModel.TimeSlot("9:00 AM", true)); 
        monSlots.add(new DoctorsModel.TimeSlot("10:00 AM", true)); 
        monSlots.add(new DoctorsModel.TimeSlot("11:30 AM", true)); 
        monSlots.add(new DoctorsModel.TimeSlot("2:00 PM", true));
        availability.put("Monday", monSlots);

        List<DoctorsModel.TimeSlot> wedSlots = new ArrayList<>();
        wedSlots.add(new DoctorsModel.TimeSlot("10:30 AM", true)); 
        wedSlots.add(new DoctorsModel.TimeSlot("12:00 PM", true)); 
        wedSlots.add(new DoctorsModel.TimeSlot("3:30 PM", true));
        availability.put("Wednesday", wedSlots);
        return availability;
    }

    private static Map<String, List<DoctorsModel.TimeSlot>> getAvailabilityNusrat() {
        Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
        List<DoctorsModel.TimeSlot> tueSlots = new ArrayList<>();
        tueSlots.add(new DoctorsModel.TimeSlot("9:30 AM", true)); 
        tueSlots.add(new DoctorsModel.TimeSlot("11:00 AM", true)); 
        tueSlots.add(new DoctorsModel.TimeSlot("1:30 PM", true)); 
        tueSlots.add(new DoctorsModel.TimeSlot("4:00 PM", true));
        availability.put("Tuesday", tueSlots);

        List<DoctorsModel.TimeSlot> thuSlots = new ArrayList<>();
        thuSlots.add(new DoctorsModel.TimeSlot("10:00 AM", true)); 
        thuSlots.add(new DoctorsModel.TimeSlot("12:30 PM", true)); 
        thuSlots.add(new DoctorsModel.TimeSlot("3:00 PM", true));
        availability.put("Thursday", thuSlots);
        return availability;
    }

    private static Map<String, List<DoctorsModel.TimeSlot>> getAvailabilityArif() {
        Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
        List<DoctorsModel.TimeSlot> monSlots = new ArrayList<>();
        monSlots.add(new DoctorsModel.TimeSlot("10:00 AM", true)); 
        monSlots.add(new DoctorsModel.TimeSlot("11:30 AM", true)); 
        monSlots.add(new DoctorsModel.TimeSlot("2:30 PM", true));
        availability.put("Monday", monSlots);

        List<DoctorsModel.TimeSlot> friSlots = new ArrayList<>();
        friSlots.add(new DoctorsModel.TimeSlot("9:00 AM", true)); 
        friSlots.add(new DoctorsModel.TimeSlot("10:30 AM", true)); 
        friSlots.add(new DoctorsModel.TimeSlot("1:00 PM", true));
        availability.put("Friday", friSlots);
        return availability;
    }

    private static Map<String, List<DoctorsModel.TimeSlot>> getAvailabilityMichael() {
        Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
        List<DoctorsModel.TimeSlot> sunSlots = new ArrayList<>();
        sunSlots.add(new DoctorsModel.TimeSlot("8:30 AM", true)); 
        sunSlots.add(new DoctorsModel.TimeSlot("10:00 AM", true)); 
        sunSlots.add(new DoctorsModel.TimeSlot("11:30 AM", true)); 
        sunSlots.add(new DoctorsModel.TimeSlot("3:00 PM", true));
        availability.put("Sunday", sunSlots);

        List<DoctorsModel.TimeSlot> tueSlots = new ArrayList<>();
        tueSlots.add(new DoctorsModel.TimeSlot("9:00 AM", true)); 
        tueSlots.add(new DoctorsModel.TimeSlot("11:00 AM", true)); 
        tueSlots.add(new DoctorsModel.TimeSlot("2:00 PM", true));
        availability.put("Tuesday", tueSlots);
        return availability;
    }

    private static Map<String, List<DoctorsModel.TimeSlot>> getAvailabilityOmar() {
        Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
        List<DoctorsModel.TimeSlot> satSlots = new ArrayList<>();
        satSlots.add(new DoctorsModel.TimeSlot("9:00 AM", true)); 
        satSlots.add(new DoctorsModel.TimeSlot("10:30 AM", true)); 
        satSlots.add(new DoctorsModel.TimeSlot("1:00 PM", true)); 
        satSlots.add(new DoctorsModel.TimeSlot("4:00 PM", true));
        availability.put("Saturday", satSlots);

        List<DoctorsModel.TimeSlot> wedSlots = new ArrayList<>();
        wedSlots.add(new DoctorsModel.TimeSlot("10:00 AM", true)); 
        wedSlots.add(new DoctorsModel.TimeSlot("12:00 PM", true)); 
        wedSlots.add(new DoctorsModel.TimeSlot("2:30 PM", true));
        availability.put("Wednesday", wedSlots);
        return availability;
    }
}

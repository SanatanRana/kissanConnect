const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');
const path = require('path');

const BASE_URL = 'http://localhost:8080/api';

async function seedData() {
  try {
    console.log('--- Registering Users dummy data ---');
    
    // Create Farmer
    const farmerRes = await axios.post(`${BASE_URL}/auth/register`, {
      name: "Raju Farmer",
      email: "raju@example.com",
      password: "password123",
      phone: "9876543210",
      role: "SELLER",
      sellerType: "FARMER"
    });
    console.log('Farmer created:', farmerRes.data);
    
    // Login Farmer for token
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      email: "raju@example.com",
      password: "password123"
    });
    const token = loginRes.data.token;
    console.log('Farmer Token Received:', token ? 'Success' : 'Fail');
    
    console.log('\n--- Adding Dummy Products ---');

    // Create a dummy image
    const imagePath = path.join(__dirname, 'dummy-photo.jpg');
    if (!fs.existsSync(imagePath)) {
        fs.writeFileSync(imagePath, 'dummy image content');
    }
    
    const formData = new FormData();
    const productData = {
        name: "Fresh Himalayan Apples",
        description: "Organic hand-picked apples directly from the farms.",
        price: 150.0,
        stock: 50,
        sellerId: 1 // assuming ID is 1 for the newly created Raju
    };

    formData.append('product', JSON.stringify(productData));
    formData.append('image', fs.createReadStream(imagePath));

    const productRes = await axios.post(`${BASE_URL}/products`, formData, {
      headers: {
        ...formData.getHeaders(),
        Authorization: `Bearer ${token}`
      }
    });

    console.log('Product added successfully!', productRes.data);

    // Fetch all products to verify route
    console.log('\n--- Testing GET /api/products ---');
    const allProducts = await axios.get(`${BASE_URL}/products`);
    console.log(`Found ${allProducts.data.length} products on the server.`);
    console.log(allProducts.data[0]);

    console.log('\n✅ All API routes tested and dummy data injected successfully!');

  } catch (err) {
    if (err.code === 'ECONNREFUSED') {
       console.error(`❌ Connection Refused! Make sure the backend microservices (Gateway, User, Product, Order) are running.`);
    } else {
       console.error(`Error: ${err.response?.data?.message || err.message}`);
    }
  }
}

seedData();
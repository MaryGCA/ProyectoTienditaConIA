const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const upload = require('../controllers/upload'); // multer

router.post('/api/productos/admin', upload.single('imagen'), adminController.crearProducto);
router.put('/api/productos/admin/:id', upload.single('imagen'), adminController.editarProducto);
router.delete('/api/productos/admin/:id', adminController.eliminarProducto);

module.exports = router;
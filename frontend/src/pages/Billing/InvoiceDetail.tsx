import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Typography, Paper, Grid, Button, Divider, CircularProgress, Chip } from '@mui/material';
import { ArrowBack, Edit, Download, Print, Email } from '@mui/icons-material';
import { fetchInvoiceById } from '../../store/slices/billingSlice';
import { RootState, AppDispatch } from '../../store';

interface InvoiceDetailParams {
  id: string;
}

const InvoiceDetail: React.FC = () => {
  const { id } = useParams<InvoiceDetailParams>();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { currentInvoice, isLoading } = useSelector((state: RootState) => state.billing);

  useEffect(() => {
    if (id) {
      dispatch(fetchInvoiceById(id));
    }
  }, [dispatch, id]);

  const handleBack = () => {
    navigate('/billing');
  };

  const handleEdit = () => {
    navigate(`/billing/invoices/${id}/edit`);
  };

  const handleDownload = () => {
    // Logique pour télécharger la facture
    console.log('Téléchargement de la facture');
  };

  const handlePrint = () => {
    // Logique pour imprimer la facture
    window.print();
  };

  const handleEmail = () => {
    // Logique pour envoyer la facture par email
    console.log('Envoi de la facture par email');
  };

  if (isLoading || !currentInvoice) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'draft':
        return 'default';
      case 'sent':
        return 'info';
      case 'paid':
        return 'success';
      case 'overdue':
        return 'error';
      case 'cancelled':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'draft':
        return 'Brouillon';
      case 'sent':
        return 'Envoyée';
      case 'paid':
        return 'Payée';
      case 'overdue':
        return 'En retard';
      case 'cancelled':
        return 'Annulée';
      default:
        return status;
    }
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Détails de la facture
        </Typography>
        <Box>
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={handleBack}
            sx={{ mr: 2 }}
          >
            Retour
          </Button>
          <Button
            variant="contained"
            startIcon={<Edit />}
            onClick={handleEdit}
            sx={{ mr: 2 }}
          >
            Modifier
          </Button>
          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={handleDownload}
            sx={{ mr: 2 }}
          >
            Télécharger
          </Button>
          <Button
            variant="outlined"
            startIcon={<Print />}
            onClick={handlePrint}
            sx={{ mr: 2 }}
          >
            Imprimer
          </Button>
          <Button
            variant="outlined"
            startIcon={<Email />}
            onClick={handleEmail}
          >
            Envoyer
          </Button>
        </Box>
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <Typography variant="h5" gutterBottom>
              Facture #{currentInvoice.invoiceNumber}
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Date: {new Date(currentInvoice.issueDate).toLocaleDateString('fr-FR')}
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Échéance: {new Date(currentInvoice.dueDate).toLocaleDateString('fr-FR')}
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Chip 
                label={getStatusText(currentInvoice.status)} 
                color={getStatusColor(currentInvoice.status) as any}
                size="small"
              />
            </Box>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="h6" gutterBottom>
              Client
            </Typography>
            <Typography variant="body1">
              {currentInvoice.customer.name}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {currentInvoice.customer.email}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {currentInvoice.customer.phone}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {currentInvoice.customer.billingAddress.address}, {currentInvoice.customer.billingAddress.city}, {currentInvoice.customer.billingAddress.postalCode}
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Détails de la facture
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Description</TableCell>
                <TableCell align="right">Quantité</TableCell>
                <TableCell align="right">Prix unitaire</TableCell>
                <TableCell align="right">Total</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {currentInvoice.items.map((item, index) => (
                <TableRow key={index}>
                  <TableCell>{item.description}</TableCell>
                  <TableCell align="right">{item.quantity}</TableCell>
                  <TableCell align="right">{item.unitPrice.toFixed(2)} €</TableCell>
                  <TableCell align="right">{(item.quantity * item.unitPrice).toFixed(2)} €</TableCell>
                </TableRow>
              ))}
              <TableRow>
                <TableCell colSpan={3} align="right">
                  <Typography variant="subtitle1">Sous-total:</Typography>
                </TableCell>
                <TableCell align="right">
                  <Typography variant="subtitle1">{currentInvoice.subtotal.toFixed(2)} €</Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell colSpan={3} align="right">
                  <Typography variant="subtitle1">TVA ({currentInvoice.taxRate}%):</Typography>
                </TableCell>
                <TableCell align="right">
                  <Typography variant="subtitle1">{currentInvoice.taxAmount.toFixed(2)} €</Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell colSpan={3} align="right">
                  <Typography variant="h6">Total:</Typography>
                </TableCell>
                <TableCell align="right">
                  <Typography variant="h6">{currentInvoice.total.toFixed(2)} €</Typography>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Notes et conditions
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Typography variant="body1" paragraph>
          {currentInvoice.notes || 'Aucune note'}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Conditions de paiement: {currentInvoice.paymentTerms}
        </Typography>
      </Paper>
    </Box>
  );
};

export default InvoiceDetail;

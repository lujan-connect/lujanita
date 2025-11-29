import { Given, When, Then } from '@cucumber/cucumber';

Given('el BFF responde 403 para la solicitud actual', function () {
  // TODO: Mockear respuesta 403
  this.pending = true;
});

When('el usuario envía un mensaje', function () {
  // TODO
  this.pending = true;
});

Then('la UI muestra un error traducido con código UI003', function () {
  // TODO: Verificar mensaje de error
  this.pending = true;
});

Then('se ofrece opción de reintento', function () {
  // TODO
  this.pending = true;
});

Given('la llamada al BFF excede el tiempo de espera', function () {
  // TODO: Mockear timeout
  this.pending = true;
});

Then('la UI muestra un error con código UI006', function () {
  // TODO
  this.pending = true;
});


# Tasks: [FEATURE NAME]

> Governed by: `.github/appmod/constitution.md`

**Created**: [DATE]
**Feature**: [Link to spec.md]

## Planned Tasks

### Foundation

- [ ] Add Spring MVC dependencies
- [ ] Replace Struts filter with Spring DispatcherServlet

### Web/API

- [ ] Implement page controller routes (`/`, `/index`, `/request`, `/saverequest`)
- [ ] Implement JSON API routes (`/listrequest`, `/listcountries`)

### Views

- [ ] Migrate `request.jsp` to plain JSP/HTML (no Struts tags)
- [ ] Migrate `listrequest.jsp` to use DataTables via CDN

### Validation

- [ ] Add minimal tests for request submission validation
- [ ] Document run instructions
